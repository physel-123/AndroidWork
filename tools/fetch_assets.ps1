# 构建大都会博物馆（CC0）节气配图池：广检索 -> 拉元数据 -> 过滤公共领域亚洲绘画 -> 按题材匹配节气
$ErrorActionPreference = "Continue"
$ProgressPreference = "SilentlyContinue"

$work = "E:\AndroidWork\tools\assets"
$imgDir = "$work\images"
New-Item -ItemType Directory -Force $imgDir | Out-Null
$API = "https://collectionapi.metmuseum.org/public/collection/v1"

function Get-Ids($q, $useDept) {
    $u = "$API/search?q=$([uri]::EscapeDataString($q))&hasImages=true"
    if ($useDept) { $u += "&departmentId=6" }
    try { return @((Invoke-RestMethod -Uri $u -TimeoutSec 40).objectIDs) } catch { return @() }
}

function Get-Obj($id) {
    try { return Invoke-RestMethod -Uri "$API/objects/$id" -TimeoutSec 25 } catch { return $null }
}

function Clean($s) {
    if (-not $s) { return "" }
    return ((($s -replace '<[^>]+>', '') -replace '\s+', ' ')).Trim()
}

# ---------- 1. 广检索建池 ----------
$queries = @(
    "landscape", "flowers", "birds", "bamboo", "snow", "moon", "lotus",
    "spring", "autumn", "winter", "summer", "river", "mountain", "tree",
    "garden", "insect", "fish", "plum", "pine", "chrysanthemum",
    "peony", "swallow", "geese", "rain", "mist", "waterfall", "cloud",
    "willow", "rice", "tea", "fan", "album leaf", "hanging scroll",
    "handscroll", "flower and bird", "landscape in snow", "spring morning",
    "autumn mountain", "moonlight", "cat", "crane", "duck", "butterfly",
    "cherry blossom", "apricot", "peach blossom", "osmanthus", "crab", "persimmon"
)
$pool = New-Object System.Collections.Generic.HashSet[int]
foreach ($q in $queries) {
    foreach ($useDept in @($true, $false)) {
        foreach ($id in (Get-Ids $q $useDept)) {
            if ($id) { [void]$pool.Add([int]$id) }
        }
    }
}
# 手工核对过的公共领域中国绘画
foreach ($id in @(36432, 53408, 53429, 41057, 40285, 36188, 37145, 36742, 53439,
                  39901, 39931, 44759, 40057, 39895, 36131, 36150, 49456, 36161,
                  57232, 36726, 701293, 53360, 53361, 36391, 42128)) {
    [void]$pool.Add([int]$id)
}
Write-Host "池子对象数: $($pool.Count)"

# ---------- 2. 拉元数据并过滤 ----------
$candidates = @()
$i = 0
foreach ($id in $pool) {
    $i++
    if ($i % 25 -eq 0) { Write-Host "  已检查 $i / $($pool.Count)" }
    $o = Get-Obj $id
    if (-not $o) { continue }
    if (-not $o.isPublicDomain) { continue }
    if (-not $o.primaryImage) { continue }
    if ($o.classification -notmatch 'Painting|Print|Calligraphy') { continue }
    if ($o.culture -notmatch 'China|Japan|Korea') { continue }
    $candidates += [pscustomobject]@{
        id       = [int]$o.objectID
        title    = Clean $o.title
        artist   = Clean $o.artistDisplayName
        date     = Clean $o.objectDate
        culture  = Clean $o.culture
        medium   = Clean $o.medium
        credit   = Clean $o.creditLine
        image    = ($o.primaryImage -replace '/original/', '/web-large/')
        haystack = (Clean "$($o.title) $($o.objectDate) $($o.medium) $($o.classification)").ToLower()
    }
}
Write-Host "可用候选（公共领域亚洲绘画）: $($candidates.Count)"

# ---------- 3. 节气 -> 关键词匹配 ----------
$plan = @{
    1  = @("plum", "willow", "spring")
    2  = @("rain", "mist", "cloud", "river")
    3  = @("bird", "insect", "flower")
    4  = @("bird", "swallow", "flower", "spring")
    5  = @("landscape", "spring", "green")
    6  = @("peony", "flower", "rain")
    7  = @("lotus", "summer", "water")
    8  = @("grain", "wheat", "field", "rice")
    9  = @("rice", "farm", "plant")
    10 = @("lotus", "summer", "water")
    11 = @("fan", "summer", "lotus")
    12 = @("waterfall", "mountain", "cloud")
    13 = @("autumn", "maple", "mist")
    14 = @("geese", "bird", "moon")
    15 = @("dew", "grass", "flower", "autumn")
    16 = @("moon", "autumn", "night")
    17 = @("chrysanthemum", "autumn", "flower")
    18 = @("persimmon", "fruit", "autumn", "tree")
    19 = @("winter", "snow", "cold")
    20 = @("bamboo", "snow", "winter")
    21 = @("snow", "mountain", "winter")
    22 = @("flower", "winter", "plum", "blossom")
    23 = @("plum", "blossom", "winter")
    24 = @("pine", "snow", "winter", "cold")
}

$used = New-Object System.Collections.Generic.HashSet[int]
$results = @()
foreach ($term in ($plan.Keys | Sort-Object)) {
    $keywords = $plan[$term]
    $scored = @()
    foreach ($c in $candidates) {
        if ($used.Contains($c.id)) { continue }
        $s = 0
        $k = 0
        foreach ($kw in $keywords) {
            if ($c.haystack -match [regex]::Escape($kw)) { $s += (10 - $k) }
            $k++
        }
        if ($s -gt 0) { $scored += [pscustomobject]@{ c = $c; s = $s } }
    }
    $pick = @($scored | Sort-Object -Property s -Descending | Select-Object -First 2)
    if ($pick.Count -eq 0) {
        foreach ($f in @($candidates | Where-Object { -not $used.Contains($_.id) } | Select-Object -First 1)) {
            $pick += [pscustomobject]@{ c = $f; s = 0 }
        }
    }
    foreach ($p in $pick) {
        [void]$used.Add($p.c.id)
        $file = "term{0:D2}_{1}.jpg" -f [int]$term, $p.c.id
        $results += [pscustomobject]@{
            term = [int]$term; objectID = $p.c.id; file = $file; url = $p.c.image
            title = $p.c.title; artist = $p.c.artist; date = $p.c.date
            culture = $p.c.culture; medium = $p.c.medium; credit = $p.c.credit
        }
        Write-Host ("[{0,2}] {1}" -f $term, $p.c.title)
    }
}

# ---------- 4. 并发下载 ----------
Write-Host "`n共 $($results.Count) 张，开始下载..."
$sw = [System.Diagnostics.Stopwatch]::StartNew()
$jobs = @()
foreach ($r in $results) {
    $jobs += Start-Job -ScriptBlock {
        param($u, $f)
        curl.exe -sS -o $f --max-time 180 -L $u 2>&1 | Out-Null
    } -ArgumentList $r.url, "$imgDir\$($r.file)"
}
$jobs | Wait-Job -Timeout 900 | Out-Null
$jobs | Remove-Job -Force -ErrorAction SilentlyContinue
$sw.Stop()

$ok = @()
foreach ($r in $results) {
    $p = "$imgDir\$($r.file)"
    if ((Test-Path $p) -and (Get-Item $p).Length -gt 8000) { $ok += $r } else { Write-Host "剔除: $($r.file)" }
}
$kb = 0
foreach ($r in $ok) { $kb += (Get-Item "$imgDir\$($r.file)").Length / 1KB }
Write-Host ("完成：{0} 张 / {1:N0} KB / {2:N1}s" -f $ok.Count, $kb, $sw.Elapsed.TotalSeconds)
$ok | ConvertTo-Json -Depth 3 | Set-Content "$work\manifest.json" -Encoding UTF8
Write-Host "清单: $work\manifest.json"
