# -*- coding: utf-8 -*-
"""
为「分工」目录下的 4 个成员文件夹生成 README.md，并生成 00_分工说明.md。
（用 Python 写 Markdown，避免 PowerShell 双引号字符串里的反引号转义问题）
"""
import os

OUT = r"E:\AndroidWork\分工"

COMMON_FILES = [
    ("MainActivity.kt", "唯一的 Activity，承载 Compose 导航与登录态判断"),
    ("JieQiApp.kt", "Application，配置 Coil 图片加载器"),
    ("data\\AppContainer.kt", "依赖容器：要在里面注册自己的 Repository"),
    ("data\\SettingsStore.kt", "DataStore：提醒设置、昵称、主题"),
    ("data\\Repositories.kt", "打卡仓库 + 收藏仓库（M2 与 M4 共用）"),
    ("data\\db\\Entities.kt", "基础表实体：打卡 / 收藏 / 民俗"),
    ("data\\db\\Daos.kt", "基础表 DAO"),
    ("data\\db\\JieQiDatabase.kt", "数据库定义：新增表时要在这里登记"),
    ("ui\\LocalProviders.kt", "CompositionLocal：依赖容器与 Snackbar"),
    ("ui\\navigation\\Navigation.kt", "路由表：新增页面时要在这里加路由"),
    ("ui\\theme\\Color.kt", "国风配色与 24 节气主题色"),
    ("ui\\theme\\Type.kt", "字体排版"),
    ("ui\\theme\\Theme.kt", "Material 3 主题"),
    ("ui\\components\\Components.kt", "通用组件：卡片 / 标签 / 进度条 / 统计格"),
    ("util\\ImageUtils.kt", "图片加载、头像、相对时间"),
    ("util\\ShareUtils.kt", "系统分享"),
    ("AndroidManifest.xml", "应用清单：权限 / Activity / 广播接收器"),
    ("res\\", "资源目录：图标、颜色、字符串、主题（含 values-night）"),
    ("app\\src\\androidTest\\...\\MainActivitySmokeTest.kt", "整机冒烟测试（跨模块的集成测试）"),
]

MEMBERS = [
    {
        "dir": "M1_成员1_节气内容与账号",
        "who": "成员 1（组长）",
        "module": "M1 节气内容展示与检索 + S0 注册 / 登录",
        "color": "竹青",
        "points": [
            "F1.1 当日节气智能推荐",
            "F1.2 节气日期天文计算与倒计时",
            "F1.3 二十四节气图谱检索",
            "F1.4 节气文化档案（三候 / 习俗 / 食俗 / 养生 / 诗词）",
            "F1.5 古画配图浏览与全屏查看",
            "S0.1 注册（账号唯一校验）",
            "S0.2 登录（加盐 SHA-256 摘要）",
            "S0.3 登录态持久化",
            "S0.4 游客浏览模式",
            "S0.5 演示账号一键切换",
        ],
        "files": [
            ("domain\\SolarTermCalculator.kt", "VSOP87D 天文算法：节气时刻求解、儒略日换算"),
            ("domain\\TermDateUtils.kt", "当前 / 下一节气、倒计时、年度节气表（带缓存）"),
            ("data\\content\\SolarTermContent.kt", "24 节气完整内容库（三候/习俗/食俗/养生/诗话）"),
            ("data\\content\\ArtworkLibrary.kt", "30 幅公共领域古画配图库（含作者与馆藏）"),
            ("data\\content\\DailyContentFactory.kt", "每日一句 / 一俗 / 一食、分享文案生成"),
            ("data\\UserRepository.kt", "注册、登录、演示账号、会话保持"),
            ("data\\SessionStore.kt", "登录态存储（DataStore）"),
            ("ui\\screens\\HomeScreen.kt", "首页：当前节气、倒计时、今日一俗、足迹"),
            ("ui\\screens\\TermListScreen.kt", "节气列表：罗盘、四季筛选、搜索"),
            ("ui\\screens\\TermDetailScreen.kt", "节气详情：文化档案 + 古画 + 讨论区"),
            ("ui\\screens\\LoginScreen.kt", "登录 / 注册页（含演示账号一键登录）"),
            ("ui\\screens\\ImageCreditsScreen.kt", "配图来源与授权页"),
            ("ui\\components\\TermWheel.kt", "节气罗盘（Canvas 自绘、点击命中）"),
            ("ui\\components\\ArtworkViews.kt", "古画展示、全屏查看组件"),
        ],
        "tests": [
            ("domain\\SolarTermCalculatorTest.kt", "天文算法精度测试（对比紫金山天文台公布值）"),
            ("domain\\TermDateUtilsTest.kt", "节气日期工具测试（跨年、时刻边界）"),
        ],
        "extra_assets": True,
    },
    {
        "dir": "M2_成员2_打卡与习惯养成",
        "who": "成员 2",
        "module": "M2 节气打卡与习惯养成",
        "color": "朱砂",
        "points": [
            "F2.1 一键打卡（含心情备注）",
            "F2.2 月度打卡日历（补打卡 / 取消）",
            "F2.3 连续打卡与成长等级（7 级）",
            "F2.4 二十四节气点亮进度",
            "F2.5 打卡卡片分享",
        ],
        "files": [
            ("domain\\CheckInCalculator.kt", "连续天数、最长纪录、等级、月历排版（纯函数）"),
            ("ui\\screens\\CheckInScreen.kt", "打卡页：统计、月历、30 天热力、记录管理"),
        ],
        "tests": [
            ("domain\\CheckInCalculatorTest.kt", "打卡统计逻辑测试（8 个用例）"),
        ],
        "extra_assets": False,
    },
    {
        "dir": "M3_成员3_广场与互动",
        "who": "成员 3",
        "module": "M3 打卡广场与社区互动",
        "color": "琥珀",
        "points": [
            "F3.1 发布图文打卡动态",
            "F3.2 动态信息流与筛选",
            "F3.3 点赞（联合主键，天然幂等）",
            "F3.4 评论与二级回复",
            "F3.5 内容删除与分享",
        ],
        "files": [
            ("data\\SocialRepository.kt", "动态、评论、点赞仓库 + 演示数据播种"),
            ("data\\db\\SocialEntities.kt", "用户 / 动态 / 评论 / 点赞 4 张表实体"),
            ("data\\db\\SocialDaos.kt", "上述 4 张表的 DAO"),
            ("ui\\screens\\FeedScreen.kt", "打卡广场：信息流、发布动态弹窗"),
            ("ui\\components\\CommentViews.kt", "评论区、点赞按钮组件"),
        ],
        "tests": [],
        "extra_assets": False,
    },
    {
        "dir": "M4_成员4_民俗地图与提醒",
        "who": "成员 4",
        "module": "M4 地域民俗地图 + M5 每日提醒与收藏",
        "color": "靛青",
        "points": [
            "F4.1 中国地图民俗分布可视化",
            "F4.2 按省份 / 节气筛选",
            "F4.3 民俗详情查看与分享",
            "F4.4 用户民俗投稿",
            "F5.1 每日定时节气提醒",
            "F5.2 提醒时刻自定义",
            "F5.3 开机自动恢复提醒",
            "F5.4 节气 / 民俗收藏",
        ],
        "files": [
            ("ui\\components\\FolkMapCanvas.kt", "中国地图绘制、经纬度投影、打点与省份点击"),
            ("ui\\components\\ProvinceOutlines.kt", "31 个省级行政区真实经纬度轮廓"),
            ("ui\\screens\\FolkMapScreen.kt", "民俗地图页：筛选、详情、投稿弹窗"),
            ("ui\\screens\\ProfileScreen.kt", "我的页：统计、提醒设置、主题、数据导出"),
            ("data\\FolkCustomRepository.kt", "民俗仓库：内置数据播种、筛选、用户投稿"),
            ("data\\content\\FolkCustomContent.kt", "34 条各地民俗内置数据（含经纬度）"),
            ("notify\\ReminderScheduler.kt", "AlarmManager 提醒调度、通知渠道"),
            ("notify\\DailyReminderReceiver.kt", "提醒接收器 + 开机恢复接收器"),
        ],
        "tests": [],
        "extra_assets": False,
    },
]


def build_readme(m):
    L = []
    L.append("# %s" % m["module"])
    L.append("")
    L.append("> 负责人：%s  " % m["who"])
    L.append("> 主题色：%s  " % m["color"])
    L.append("> 本文件夹全部**复制自总工程**，原工程未做任何改动。")
    L.append("")
    L.append("---")
    L.append("")
    L.append("## 一、你负责什么")
    L.append("")
    for p in m["points"]:
        L.append("- %s" % p)
    L.append("")
    L.append("---")
    L.append("")
    L.append("## 二、本人负责的文件（在「01_本人负责」里）")
    L.append("")
    L.append("| 文件（相对 `com/example/jieqi/`） | 作用 |")
    L.append("|---|---|")
    for rel, desc in m["files"]:
        L.append("| `%s` | %s |" % (rel, desc))
    for rel, desc in m["tests"]:
        L.append("| `%s`（测试） | %s |" % (rel, desc))
    L.append("")
    L.append("路径已保留完整层级，直接把「01_本人负责/app/」整个覆盖到你工程的 app/ 即可。")
    L.append("")
    L.append("## 三、公共依赖（在「02_公共依赖」里）")
    L.append("")
    L.append("这些文件**不属于你一个人**，但你的代码需要它们才能编译：")
    L.append("")
    L.append("| 文件 | 说明 |")
    L.append("|---|---|")
    for rel, desc in COMMON_FILES:
        L.append("| `%s` | %s |" % (rel, desc))
    L.append("")
    L.append("另外还包含：`settings.gradle.kts`、`build.gradle.kts`、`gradle.properties`、")
    L.append("`gradlew` / `gradlew.bat`、`gradle/wrapper/`、`app/build.gradle.kts`、`app/proguard-rules.pro`。")
    L.append("")
    L.append("> **约定**：「02_公共依赖」里的文件以组长维护的版本为准。")
    L.append("> 当你需要新增 Repository、新增路由、新增数据库表时，")
    L.append("> 先把改动发给组长合并，再同步回自己的工程，**不要各自维护一份**，否则最后无法合并。")
    L.append("")
    if m["extra_assets"]:
        L.append("## 四、图片资源（在「03_图片资源」里）")
        L.append("")
        L.append("30 幅公共领域古画（约 3.7 MB，来自大都会艺术博物馆 Open Access，CC0）：")
        L.append("")
        L.append("```")
        L.append("03_图片资源/app/src/main/assets/images/*.jpg")
        L.append("```")
        L.append("")
        L.append("这些图片由 M1 的 `ArtworkLibrary.kt` 引用，")
        L.append("**其他成员的工作区也需要这份资源**，否则图片显示不出来。")
        L.append("")
        L.append("## 五、怎么用")
    else:
        L.append("## 四、怎么用")
    L.append("")
    L.append("## 四、怎么用")
    L.append("")
    L.append("1. 用「02_公共依赖」当作基础工程（含 app/、构建脚本、gradle wrapper、res/）")
    L.append("2. 把「01_本人负责/app/」覆盖进去")
    if m["extra_assets"]:
        L.append("3. 把「03_图片资源/app/」覆盖进去（古画配图）")
        L.append("4. 用 Android Studio 打开，Sync 后即可编译")
        L.append("5. 只改「01_本人负责」里对应的文件；需要动公共文件时按上面的约定走")
        L.append("")
        L.append("## 五、验收标准")
    else:
        L.append("3. 从 M1 的「03_图片资源」拷一份 assets/images 进来（否则古画不显示）")
        L.append("4. 用 Android Studio 打开，Sync 后即可编译")
        L.append("5. 只改「01_本人负责」里对应的文件；需要动公共文件时按上面的约定走")
        L.append("")
        L.append("## 五、验收标准")
    L.append("")
    L.append("- 你负责的页面能正常打开、功能可用")
    L.append("- `gradlew.bat :app:assembleDebug` 编译通过")
    L.append("- `gradlew.bat :app:lintDebug` 无错误")
    if m["tests"]:
        L.append("- `gradlew.bat :app:testDebugUnitTest` 全绿")
    L.append("")
    return "\n".join(L)


def build_overview():
    L = []
    L.append("# 四人分工文件交接说明")
    L.append("")
    L.append("> 来源工程：`E:\\AndroidWork`（**原工程未做任何改动，这里全部是复制**）")
    L.append("")
    L.append("## 目录结构")
    L.append("")
    L.append("```")
    L.append("分工\\")
    L.append("├── 00_分工说明.md                      本文件")
    L.append("├── M1_成员1_节气内容与账号\\            成员 1（组长）：M1 内容与检索 + 注册登录")
    L.append("├── M2_成员2_打卡与习惯养成\\            成员 2：M2 打卡与成长体系")
    L.append("├── M3_成员3_广场与互动\\                成员 3：M3 打卡广场与社区互动")
    L.append("└── M4_成员4_民俗地图与提醒\\            成员 4：M4 民俗地图 + M5 提醒与收藏")
    L.append("```")
    L.append("")
    L.append("每个成员文件夹内：")
    L.append("")
    L.append("| 子目录 | 内容 |")
    L.append("|---|---|")
    L.append("| `01_本人负责/` | 该成员**独占**的源码（含单元测试），保留 `app/src/main/java/com/example/jieqi/` 完整路径 |")
    L.append("| `02_公共依赖/` | 编译必需的公共文件（入口、导航、数据库、主题、构建脚本、res、清单），**以组长版本为准** |")
    L.append("| `03_图片资源/` | 仅 M1 有：30 幅公共领域古画（约 3.7 MB） |")
    L.append("")
    L.append("## 文件归属总览")
    L.append("")
    L.append("| 成员 | 模块 | 独占文件 | 公共文件 |")
    L.append("|---|---|---|---|")
    L.append("| 成员 1（组长） | M1 + S0 | 16 个（14 源码 + 2 测试） | 17 + res + 构建脚本 |")
    L.append("| 成员 2 | M2 | 3 个（2 源码 + 1 测试） | 同上 |")
    L.append("| 成员 3 | M3 | 5 个源码 | 同上 |")
    L.append("| 成员 4 | M4 + M5 | 8 个源码 | 同上 |")
    L.append("")
    L.append("合计 49 个 Kotlin 文件全部有归属，无遗漏。")
    L.append("")
    L.append("## 三条协作铁律")
    L.append("")
    L.append("1. **公共文件只有一份权威版本**（组长维护）。组员需要新增 Repository、路由、数据库表时，")
    L.append("   先提给组长，合并后再各自同步。")
    L.append("2. **只在自己负责的文件里写业务代码**，避免同一文件多人修改。")
    L.append("3. 合并前每人都要保证 `assembleDebug` 能过，再提交。")
    L.append("")
    L.append("## 已知耦合点（需要四人协调）")
    L.append("")
    L.append("| 文件 | 涉及成员 | 说明 |")
    L.append("|---|---|---|")
    L.append("| `data\\Repositories.kt` | M2 + M4 | 同时含 `CheckInRepository`（打卡）与 `FavoriteRepository`（收藏） |")
    L.append("| `data\\db\\Entities.kt`、`Daos.kt` | M2 + M4 | `check_in` 表属打卡；`favorite`、`folk_custom` 表属收藏与地图 |")
    L.append("| `ui\\screens\\ProfileScreen.kt` | M4 | 我的页含提醒设置（M5）与各项统计（依赖 M1/M2/M3 的数据） |")
    L.append("| `ui\\navigation\\Navigation.kt` | 全体 | 每人新增页面时都要改路由表 |")
    L.append("| `data\\AppContainer.kt` | 全体 | 每人注册自己的 Repository 时都要改 |")
    L.append("")
    return "\n".join(L)


if __name__ == "__main__":
    for m in MEMBERS:
        p = os.path.join(OUT, m["dir"], "README.md")
        if not os.path.isdir(os.path.dirname(p)):
            print("跳过（目录不存在）:", m["dir"])
            continue
        with open(p, "w", encoding="utf-8", newline="\n") as fh:
            fh.write(build_readme(m))
        print("已生成", p)
    p = os.path.join(OUT, "00_分工说明.md")
    with open(p, "w", encoding="utf-8", newline="\n") as fh:
        fh.write(build_overview())
    print("已生成", p)
