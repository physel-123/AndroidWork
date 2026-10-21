# 把工程按四人分工拆成 4 个交接文件夹（只复制，不动原工程）
# 用法： powershell -File E:\AndroidWork\tools\split_by_member.ps1
$ErrorActionPreference = "Continue"

$PROJ = "E:\AndroidWork"
$SRC_MAIN = "$PROJ\app\src\main"
$SRC_TEST = "$PROJ\app\src\test"
$OUT = "$PROJ\分工"

# ---------------------------------------------------------------- 文件归属表
# 公共依赖：每个工作区都必须有，且以组长维护的版本为准
$COMMON = @(
    "java\com\example\jieqi\MainActivity.kt",
    "java\com\example\jieqi\JieQiApp.kt",
    "java\com\example\jieqi\data\AppContainer.kt",
    "java\com\example\jieqi\data\SettingsStore.kt",
    "java\com\example\jieqi\data\Repositories.kt",
    "java\com\example\jieqi\data\db\Entities.kt",
    "java\com\example\jieqi\data\db\Daos.kt",
    "java\com\example\jieqi\data\db\JieQiDatabase.kt",
    "java\com\example\jieqi\ui\LocalProviders.kt",
    "java\com\example\jieqi\ui\navigation\Navigation.kt",
    "java\com\example\jieqi\ui\theme\Color.kt",
    "java\com\example\jieqi\ui\theme\Type.kt",
    "java\com\example\jieqi\ui\theme\Theme.kt",
    "java\com\example\jieqi\ui\components\Components.kt",
    "java\com\example\jieqi\util\ImageUtils.kt",
    "java\com\example\jieqi\util\ShareUtils.kt",
    "AndroidManifest.xml"
)

$MEMBERS = @(
    @{
        key    = "M1"
        dir    = "M1_成员1_节气内容与账号"
        who    = "成员 1（组长）"
        module = "M1 节气内容展示与检索 + S0 注册 / 登录"
        color  = "竹青"
        points = @(
            "F1.1 当日节气智能推荐",
            "F1.2 节气日期天文计算与倒计时",
            "F1.3 二十四节气图谱检索",
            "F1.4 节气文化档案（三候 / 习俗 / 食俗 / 养生 / 诗词）",
            "F1.5 古画配图浏览与全屏查看",
            "S0.1 注册（账号唯一校验）",
            "S0.2 登录（加盐 SHA-256 摘要）",
            "S0.3 登录态持久化",
            "S0.4 游客浏览模式",
            "S0.5 演示账号一键切换"
        )
        files  = @(
            @("domain\SolarTermCalculator.kt", "VSOP87D 天文算法：节气时刻求解、儒略日换算"),
            @("domain\TermDateUtils.kt", "当前 / 下一节气、倒计时、年度节气表（带缓存）"),
            @("data\content\SolarTermContent.kt", "24 节气完整内容库"),
            @("data\content\ArtworkLibrary.kt", "30 幅公共领域古画配图库"),
            @("data\content\DailyContentFactory.kt", "每日一句 / 一俗 / 一食、分享文案"),
            @("data\UserRepository.kt", "注册、登录、演示账号、会话"),
            @("data\SessionStore.kt", "登录态存储（DataStore）"),
            @("ui\screens\HomeScreen.kt", "首页：当前节气、倒计时、今日一俗"),
            @("ui\screens\TermListScreen.kt", "节气列表：罗盘、四季筛选、搜索"),
            @("ui\screens\TermDetailScreen.kt", "节气详情：文化档案 + 古画 + 讨论区"),
            @("ui\screens\LoginScreen.kt", "登录 / 注册页"),
            @("ui\screens\ImageCreditsScreen.kt", "配图来源与授权页"),
            @("ui\components\TermWheel.kt", "节气罗盘（Canvas 自绘）"),
            @("ui\components\ArtworkViews.kt", "古画展示 / 全屏查看组件")
        )
        tests  = @(
            @("domain\SolarTermCalculatorTest.kt", "天文算法精度测试（对比紫金山天文台公布值）"),
            @("domain\TermDateUtilsTest.kt", "节气日期工具测试（跨年、时刻边界）")
        )
        assets = $true
    },
    @{
        key    = "M2"
        dir    = "M2_成员2_打卡与习惯养成"
        who    = "成员 2"
        module = "M2 节气打卡与习惯养成"
        color  = "朱砂"
        points = @(
            "F2.1 一键打卡（含心情备注）",
            "F2.2 月度打卡日历（补打卡 / 取消）",
            "F2.3 连续打卡与成长等级（7 级）",
            "F2.4 二十四节气点亮进度",
            "F2.5 打卡卡片分享"
        )
        files  = @(
            @("domain\CheckInCalculator.kt", "连续天数、最长纪录、等级、月历排版（纯函数）"),
            @("ui\screens\CheckInScreen.kt", "打卡页：统计、月历、记录管理")
        )
        tests  = ,@(
            @("domain\CheckInCalculatorTest.kt", "打卡统计逻辑测试（8 个用例）")
        )
        assets = $false
    },
    @{
        key    = "M3"
        dir    = "M3_成员3_广场与互动"
        who    = "成员 3"
        module = "M3 打卡广场与社区互动"
        color  = "琥珀"
        points = @(
            "F3.1 发布图文打卡动态",
            "F3.2 动态信息流与筛选",
            "F3.3 点赞（联合主键，幂等）",
            "F3.4 评论与二级回复",
            "F3.5 内容删除与分享"
        )
        files  = @(
            @("data\SocialRepository.kt", "动态、评论、点赞仓库 + 演示数据播种"),
            @("data\db\SocialEntities.kt", "用户 / 动态 / 评论 / 点赞 4 张表实体"),
            @("data\db\SocialDaos.kt", "上述 4 张表的 DAO"),
            @("ui\screens\FeedScreen.kt", "打卡广场：信息流、发布弹窗"),
            @("ui\components\CommentViews.kt", "评论区、点赞按钮组件")
        )
        tests  = @()
        assets = $false
    },
    @{
        key    = "M4"
        dir    = "M4_成员4_民俗地图与提醒"
        who    = "成员 4"
        module = "M4 地域民俗地图 + M5 每日提醒与收藏"
        color  = "靛青"
        points = @(
            "F4.1 中国地图民俗分布可视化",
            "F4.2 按省份 / 节气筛选",
            "F4.3 民俗详情查看与分享",
            "F4.4 用户民俗投稿",
            "F5.1 每日定时节气提醒",
            "F5.2 提醒时刻自定义",
            "F5.3 开机自动恢复提醒",
            "F5.4 节气 / 民俗收藏"
        )
        files  = @(
            @("ui\components\FolkMapCanvas.kt", "中国地图绘制、经纬度投影、打点与省份点击"),
            @("ui\components\ProvinceOutlines.kt", "31 个省级行政区真实经纬度轮廓"),
            @("ui\screens\FolkMapScreen.kt", "民俗地图页：筛选、详情、投稿弹窗"),
            @("ui\screens\ProfileScreen.kt", "我的页：统计、提醒设置、主题、数据导出"),
            @("data\FolkCustomRepository.kt", "民俗仓库：内置数据播种、筛选、用户投稿"),
            @("data\content\FolkCustomContent.kt", "34 条各地民俗内置数据"),
            @("notify\ReminderScheduler.kt", "AlarmManager 提醒调度、通知渠道"),
            @("notify\DailyReminderReceiver.kt", "提醒接收器 + 开机恢复接收器")
        )
        tests  = @()
        assets = $false
    }
)

$PKG = "java\com\example\jieqi"

function New-TextFile($path, $content) {
    $dir = Split-Path $path -Parent
    if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Force $dir | Out-Null }
    [System.IO.File]::WriteAllText($path, $content, (New-Object System.Text.UTF8Encoding($false)))
}

if (Test-Path $OUT) { Remove-Item $OUT -Recurse -Force }
New-Item -ItemType Directory -Force $OUT | Out-Null

$summary = @()

foreach ($m in $MEMBERS) {
    $base = Join-Path $OUT $m.dir
    $ownDir = Join-Path $base "01_本人负责"
    $commonDir = Join-Path $base "02_公共依赖"
    New-Item -ItemType Directory -Force $ownDir, $commonDir | Out-Null

    # ---- 本人负责的源码 ----
    $copied = 0
    foreach ($pair in $m.files) {
        $rel, $desc = $pair[0], $pair[1]
        $src = Join-Path $SRC_MAIN "$PKG\$rel"
        $dst = Join-Path $ownDir "app\src\main\$PKG\$rel"
        if (Test-Path $src) {
            $d = Split-Path $dst -Parent
            if (-not (Test-Path $d)) { New-Item -ItemType Directory -Force $d | Out-Null }
            Copy-Item $src $dst -Force
            $copied++
        } else {
            Write-Host "  [警告] 找不到 $rel"
        }
    }
    foreach ($pair in $m.tests) {
        $rel, $desc = $pair[0], $pair[1]
        $src = Join-Path $SRC_TEST "java\com\example\jieqi\$rel"
        $dst = Join-Path $ownDir "app\src\test\java\com\example\jieqi\$rel"
        $d = Split-Path $dst -Parent
        if (-not (Test-Path $d)) { New-Item -ItemType Directory -Force $d | Out-Null }
        if (Test-Path $src) {
            Copy-Item $src $dst -Force
            $copied++
        }
    }

    # ---- 公共依赖 ----
    foreach ($rel in $COMMON) {
        $src = Join-Path $SRC_MAIN $rel
        $dst = Join-Path $commonDir "app\src\main\$rel"
        $d = Split-Path $dst -Parent
        if (-not (Test-Path $d)) { New-Item -ItemType Directory -Force $d | Out-Null }
        if (Test-Path $src) { Copy-Item $src $dst -Force }
    }
    # 资源目录
    Copy-Item "$SRC_MAIN\res" (Join-Path $commonDir "app\src\main\") -Recurse -Force
    # 整机冒烟测试（跨模块，属公共集成测试）
    $smoke = "$PROJ\app\src\androidTest\java\com\example\jieqi\MainActivitySmokeTest.kt"
    if (Test-Path $smoke) {
        $dstSmoke = Join-Path $commonDir "app\src\androidTest\java\com\example\jieqi\MainActivitySmokeTest.kt"
        $dSmoke = Split-Path $dstSmoke -Parent
        if (-not (Test-Path $dSmoke)) { New-Item -ItemType Directory -Force $dSmoke | Out-Null }
        Copy-Item $smoke $dstSmoke -Force
    }
    # 构建文件
    foreach ($f in @("settings.gradle.kts", "build.gradle.kts", "gradle.properties", "gradlew", "gradlew.bat")) {
        $p = Join-Path $PROJ $f
        if (Test-Path $p) { Copy-Item $p $commonDir -Force }
    }
    Copy-Item "$PROJ\gradle" $commonDir -Recurse -Force
    Copy-Item "$PROJ\app\build.gradle.kts" (Join-Path $commonDir "app\") -Force
    Copy-Item "$PROJ\app\proguard-rules.pro" (Join-Path $commonDir "app\") -Force

    # ---- 图片资源（只有 M1 需要）----
    if ($m.assets) {
        $assetDir = Join-Path $base "03_图片资源\app\src\main\assets\images"
        New-Item -ItemType Directory -Force $assetDir | Out-Null
        Copy-Item "$SRC_MAIN\assets\images\*.jpg" $assetDir -Force
    }

    # ---- 本人文件夹的 README ----
    $fileList = ""
    foreach ($pair in $m.files) { $fileList += ("| ``{0}`` | {1} |`n" -f $pair[0], $pair[1]) }
    foreach ($pair in $m.tests) { $fileList += ("| ``{0}``（测试） | {1} |`n" -f $pair[0], $pair[1]) }
    $pointList = ""
    foreach ($p in $m.points) { $pointList += "- $p`n" }

    # README 由 tools\make_member_readmes.py 生成（避免 PowerShell 转义问题）
    $summary += ("{0,-26} 本人文件 {1,2} 个   公共依赖 {2} 个   图片 {3}" -f `
        $m.dir, $copied, $COMMON.Count, $(if ($m.assets) { "30 张" } else { "-" }))
    Write-Host ("[完成] {0}  -> 本人 {1} 个文件" -f $m.dir, $copied)
}

# ---------------------------------------------------------------- 总说明
# 00_分工说明.md 同样由 Python 脚本生成
Write-Host "`n================ 汇总 ================"
$summary | ForEach-Object { Write-Host $_ }
$sz = (Get-ChildItem $OUT -Recurse -File | Measure-Object Length -Sum).Sum / 1MB
Write-Host ("`n输出目录: {0}    总大小: {1:N1} MB" -f $OUT, $sz)
