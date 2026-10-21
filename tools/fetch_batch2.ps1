# 拉取第二批（补充题材）公共领域图片及其元数据
$ErrorActionPreference = "Continue"
$ProgressPreference = "SilentlyContinue"

$work = "E:\AndroidWork\tools\assets"
$imgDir = "$work\images"
New-Item -ItemType Directory -Force $imgDir | Out-Null
$API = "https://collectionapi.metmuseum.org/public/collection/v1"

$ids = @(
    53222, 53187, 49068, 39993, 53427, 902217, 49187, 816191, 733847,
    48949, 36049, 41485, 48966, 49150, 49133, 36437, 53892,
    49475, 40080, 816189, 53226, 45682, 53633, 39668, 73159
)

function Clean($s) {
    if (-not $s) { return "" }
    return ((($s -replace '<[^>]+>', '') -replace '\s+', ' ')).Trim()
}
function Get-ObjWithRetry($id) {
    for ($a = 1; $a -le 4; $a++) {
        try {
            $r = Invoke-RestMethod -Uri "$API/objects/$id" -TimeoutSec 30
            if ($r -and $r.objectID) { return $r }
        } catch { Start-Sleep -Milliseconds (600 * $a) }
        Start-Sleep -Milliseconds 700
    }
    return $null
}

$list = @()
$failed = @()
foreach ($id in $ids) {
    if (Test-Path "$imgDir\met_$id.jpg") { Write-Host "  [$id] 已存在，跳过"; continue }
    $o = Get-ObjWithRetry $id
    if (-not $o) { $failed += $id; Write-Host "  [$id] 失败"; Start-Sleep -Milliseconds 1000; continue }
    if (-not $o.isPublicDomain -or -not $o.primaryImage) { Write-Host "  [$id] 不可用"; Start-Sleep -Milliseconds 500; continue }
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
    Start-Sleep -Milliseconds 550
}
Write-Host "元数据可用: $($list.Count)"

if ($list.Count -gt 0) {
    $jobs = @()
    foreach ($r in $list) {
        $jobs += Start-Job -ScriptBlock {
            param($u, $f)
            curl.exe -sS -o $f --max-time 180 -L $u 2>&1 | Out-Null
        } -ArgumentList $r.image, "$imgDir\met_$($r.id).jpg"
    }
    $jobs | Wait-Job -Timeout 600 | Out-Null
    $jobs | Remove-Job -Force -ErrorAction SilentlyContinue

    $ok = @()
    foreach ($r in $list) {
        $p = "$imgDir\met_$($r.id).jpg"
        if ((Test-Path $p) -and (Get-Item $p).Length -gt 8000) {
            $r | Add-Member -NotePropertyName bytes -NotePropertyValue (Get-Item $p).Length -Force
            $ok += $r
        }
    }
    Write-Host "下载成功: $($ok.Count)"
    foreach ($r in $ok) { Write-Host ("  met_{0}.jpg | {1} | {2} | {3}" -f $r.id, $r.culture, $r.date, $r.title) }
    $ok | ConvertTo-Json -Depth 3 | Set-Content "$work\manifest_batch2.json" -Encoding UTF8
}
if ($failed.Count -gt 0) { Write-Host "失败: $($failed -join ', ')" }
