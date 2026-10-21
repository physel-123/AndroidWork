# 用必应图片补齐节气配图（缺失题材），下载后写出来源清单
$ErrorActionPreference = "Continue"
$ProgressPreference = "SilentlyContinue"

$work = "E:\AndroidWork\tools\assets"
$outDir = "$work\bing"
New-Item -ItemType Directory -Force $outDir | Out-Null

$ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Safari/537.36"

# 文件名前缀 -> 中文检索词
$targets = [ordered]@{
    lotus      = "荷花 池塘 高清"
    chrys      = "菊花 特写 高清"
    bamboo     = "竹子 雪 高清"
    rice       = "稻田 金黄 高清"
    wheat      = "麦田 麦穗 高清"
    snow       = "雪景 山水 高清"
    pine       = "松树 雪 高清"
    frog       = "青蛙 荷叶 高清"
    maple      = "枫叶 红 高清"
    persimmon  = "柿子 树枝 高清"
    osmanthus  = "桂花 特写 高清"
    swallow    = "燕子 春天 高清"
    swallowtail= "蝴蝶 花 高清"
    plum       = "梅花 雪 高清"
    camellia   = "山茶花 高清"
    peak       = "云海 山峰 高清"
}

function Search-BingImages($query, $count) {
    $url = "https://cn.bing.com/images/search?q=$([uri]::EscapeDataString($query))&form=HDRSC2&first=1"
    try {
        $html = curl.exe -sS --max-time 35 -A $ua -H "Referer: https://cn.bing.com/" $url 2>&1 | Out-String
    } catch { return @() }
    if ($html.Length -lt 5000) { return @() }
    $urls = @()
    foreach ($m in [regex]::Matches($html, 'murl&quot;:&quot;(.*?)&quot;')) {
        $u = $m.Groups[1].Value
        if ($u -match '^https?://' -and $u -notmatch '\.gif$') { $urls += $u }
    }
    return @($urls | Select-Object -Unique | Select-Object -First $count)
}

$manifest = @()
foreach ($key in $targets.Keys) {
    $query = $targets[$key]
    $urls = Search-BingImages $query 4
    Write-Host "[$key] $query -> $($urls.Count) 个候选"
    $idx = 0
    foreach ($u in $urls) {
        $idx++
        $file = "$key`_$idx.jpg"
        $path = "$outDir\$file"
        curl.exe -sS -o $path --max-time 60 -L -A $ua -H "Referer: https://cn.bing.com/" $u 2>&1 | Out-Null
        if ((Test-Path $path) -and (Get-Item $path).Length -gt 30000) {
            $manifest += [pscustomobject]@{
                key   = $key
                query = $query
                file  = $file
                url   = $u
                bytes = (Get-Item $path).Length
            }
            Write-Host ("    OK  {0}  {1:N0} KB" -f $file, ((Get-Item $path).Length / 1KB))
        } else {
            if (Test-Path $path) { Remove-Item $path -Force }
        }
        Start-Sleep -Milliseconds 250
    }
    Start-Sleep -Milliseconds 400
}

Write-Host "`n共下载 $($manifest.Count) 张"
$manifest | ConvertTo-Json -Depth 3 | Set-Content "$work\bing_manifest.json" -Encoding UTF8
$total = 0
foreach ($m in $manifest) { $total += $m.bytes }
Write-Host ("总大小: {0:N0} KB" -f ($total / 1KB))
