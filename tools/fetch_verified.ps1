# 抓取已验证 id 的公共领域图片（带退避重试，避免被 Met API 限流）
$ErrorActionPreference = "Continue"
$ProgressPreference = "SilentlyContinue"

$work = "E:\AndroidWork\tools\assets"
$imgDir = "$work\images"
New-Item -ItemType Directory -Force $imgDir | Out-Null
$API = "https://collectionapi.metmuseum.org/public/collection/v1"

$ids = @(
    36432, 53408, 41057, 40285, 39901,
    36188, 37145, 36131, 36150, 39931, 44759, 40057, 39895,
    57232, 36726, 49456, 36161, 701293,
    36742, 53439, 53360, 53361, 36391, 42128
)

function Clean($s) {
    if (-not $s) { return "" }
    return ((($s -replace '<[^>]+>', '') -replace '\s+', ' ')).Trim()
}

# 带退避重试的元数据获取
function Get-ObjWithRetry($id) {
    for ($attempt = 1; $attempt -le 4; $attempt++) {
        try {
            $r = Invoke-RestMethod -Uri "$API/objects/$id" -TimeoutSec 30
            if ($r -and $r.objectID) { return $r }
        } catch {
            Start-Sleep -Milliseconds (600 * $attempt)
        }
        Start-Sleep -Milliseconds 800
    }
    return $null
}

$list = @()
$failed = @()
Write-Host "拉取元数据（请求间隔 0.6s，失败自动重试）…"
foreach ($id in $ids) {
    $o = Get-ObjWithRetry $id
    if (-not $o) { $failed += $id; Write-Host "  [$id] 仍失败"; Start-Sleep -Milliseconds 1200; continue }
    if (-not $o.isPublicDomain -or -not $o.primaryImage) {
        Write-Host "  [$id] 非公共领域或无图，跳过"; Start-Sleep -Milliseconds 600; continue
    }
    $list += [pscustomobject]@{
        id      = [int]$o.objectID
        title   = Clean $o.title
        artist  = Clean $o.artistDisplayName
        date    = Clean $o.objectDate
        culture = Clean $o.culture
        medium  = Clean $o.medium
        credit  = Clean $o.creditLine
        image   = ($o.primaryImage -replace '/original/', '/web-large/')
    }
    Start-Sleep -Milliseconds 600
}
Write-Host "元数据可用: $($list.Count) 张（失败 $($failed.Count) 个）"

if ($list.Count -gt 0) {
    Write-Host "下载中…"
    $sw = [System.Diagnostics.Stopwatch]::StartNew()
    $jobs = @()
    foreach ($r in $list) {
        $jobs += Start-Job -ScriptBlock {
            param($u, $f)
            curl.exe -sS -o $f --max-time 180 -L $u 2>&1 | Out-Null
        } -ArgumentList $r.image, "$imgDir\met_$($r.id).jpg"
    }
    $jobs | Wait-Job -Timeout 600 | Out-Null
    $jobs | Remove-Job -Force -ErrorAction SilentlyContinue
    $sw.Stop()

    $ok = @()
    foreach ($r in $list) {
        $p = "$imgDir\met_$($r.id).jpg"
        if ((Test-Path $p) -and (Get-Item $p).Length -gt 8000) {
            $r | Add-Member -NotePropertyName bytes -NotePropertyValue (Get-Item $p).Length -Force
            $ok += $r
        }
    }
    $kb = 0
    foreach ($r in $ok) { $kb += $r.bytes / 1KB }
    Write-Host ("下载完成：{0} 张 / {1:N0} KB / {2:N1}s" -f $ok.Count, $kb, $sw.Elapsed.TotalSeconds)
    if (Test-Path "$work\manifest.json") { Remove-Item "$work\manifest.json" -Force }
    $ok | ConvertTo-Json -Depth 3 | Set-Content "$work\manifest.json" -Encoding UTF8
    foreach ($r in $ok) { Write-Host ("  met_{0}.jpg | {1} | {2}" -f $r.id, $r.culture, $r.title) }
}

if ($failed.Count -gt 0) { Write-Host "`n失败 id（可稍后重跑本脚本补齐）: $($failed -join ', ')" }
