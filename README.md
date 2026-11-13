# 节气民俗 —— 传统民俗节气文化宣传 App

> 课程：移动互联网应用开发技术　|　类型：文化传承内容型　|　平台：Android（Kotlin + Jetpack Compose）＋ 后端（Python + FastAPI）

一个可「每天打开一次」的二十四节气与地方民俗科普 App：每日节气知识推送、节气罗盘可视化、
打卡与分享、地域民俗地图，把传统文化做成**可 daily 触达、可互动、有颜值**的产品。
并在此基础上扩展了「神秘文化」板块（今日运势、灵签占卜、塔罗牌阵、手相/面相 AI 识读），
后端采用 FastAPI + PostgreSQL + Docker 部署，多模态识别接入通义千问 Qwen-VL。

---

## 一、快速开始

### 1. 环境要求

| 项目 | 版本 | 说明 |
| --- | --- | --- |
| Android Studio | 2026.1.1 及以上 | 本工程在 Android Studio 2026.1.1 下验证 |
| Gradle | 9.4.1 | 已随工程提供 wrapper |
| AGP | 9.2.1 | 见 `build.gradle.kts` |
| JDK | 17 及以上 | 推荐直接使用 Android Studio 自带 JBR 21 |
| compileSdk / targetSdk | 36 | 本机 SDK 已安装 `android-36` 平台即可 |
| minSdk | 26（Android 8.0） | 使用 `java.time` 时间 API |

### 2. 运行步骤

1. 用 Android Studio 打开本目录（`File → Open`，选择包含 `settings.gradle.kts` 的根目录）。
2. 等待 Gradle Sync 完成（首次会下载依赖，约需几分钟）。
3. 顶部工具栏选择 `app` 配置，点击 ▶ Run，安装到真机或模拟器。

> 若 Sync 报 “Minimum supported Gradle version”，请确认 `gradle/wrapper/gradle-wrapper.properties`
> 中的版本为 9.4.1；若报 JDK 版本错误，请到 `Settings → Build → Build Tools → Gradle → Gradle JDK`
> 选择 **jbr-21**（Android Studio 自带）。

### 3. 命令行构建（可选）

```bash
# Windows（使用 Android Studio 自带 JBR 作为构建 JDK）
set JAVA_HOME=D:\Android Studio\jbr

gradlew.bat :app:assembleDebug          # 生成 debug APK
gradlew.bat :app:testDebugUnitTest      # 运行单元测试（23 个用例）
gradlew.bat :app:lintDebug              # 静态检查
gradlew.bat :app:assembleRelease        # 生成 release APK
```

产物路径：`app/build/outputs/apk/debug/app-debug.apk`

> ⚠️ **若 Android Studio Sync 卡在 “Downloading …gradle-x.x.x-bin.zip”**
>
> 这是国内最常见的问题，原因有两个：`services.gradle.org` 会 307 跳转到 GitHub（国内常超时），
> 以及 AGP 9.2.1 **要求 Gradle ≥ 9.4.1**（低了会直接报错）。
>
> 本工程已在 `gradle/wrapper/gradle-wrapper.properties` 中指定 **腾讯云镜像 + 官方 SHA-256 校验**，
> 实测 43 MB/s 且与官方包逐字节一致。若你的 Studio 仍去下别的版本，请检查：
> `Settings → Build, Execution, Deployment → Build Tools → Gradle`，
> 把 **Distribution** 选为 `Use Gradle from: gradle-wrapper.properties file`（默认即是）。

---

## 二、功能清单

### 1. 首页（每日触达入口）
- 按时间段变化的问候语 + 当前节气主卡片（节气名、释义、精确时刻、序号 x/24）
- **距离下一个节气的倒计时**（精确到小时/分钟）
- 一键打卡（可直接写一句心情）与连续打卡天数
- 今日一俗、今日宜食、三候物候、顺时养生、节气诗话
- 我的节气足迹：累计打卡 / 点亮节气进度条 / 收藏数 / 民俗条目数

### 2. 节气（内容浏览与检索）
- **节气罗盘**：Canvas 手绘的 24 节气环形可视化，当前节气高亮在 12 点方向，点击任一节气直达详情
- 春/夏/秋/冬四季筛选，关键词搜索（节气名、拼音、习俗、食俗关键词，如“饺子”“踏青”）
- 24 张节气卡片，当前节气带「当前」标记

### 3. 节气详情（文化档案）
- 精确时刻（由天文算法实时计算，非查表）、黄经度数、传统日期区间
- **节气画卷**：1—2 幅中国古代/日本绘画（CC0 公共领域藏品），可全屏放大看，附作者与馆藏署名
- 由来与含义、三候物候、传统习俗（逐条编号）、时令食俗、顺时养生、节气诗话
- 该节气相关的各地民俗条目，可一键跳转民俗地图
- **节气讨论**：可对节气发表评论与回复
- 收藏、分享（系统分享面板）

### 4. 打卡广场（互动模块，已上后端）
- **发布动态**：写正文、选心情标签、从该节气配画中挑一张作为配图
- **信息流**：所有用户的打卡动态（存后端 PostgreSQL，多设备共享），支持「全部动态 / 我的动态」筛选
- **点赞**：真实点赞记录（幂等，重复点击即取消），存后端
- **评论与回复**：一级评论 + 二级回复，可删除自己的评论，存后端
- **分享动态**：一键分享到系统分享面板
- 顶部展示社区成员数与动态总数

### 5. 账号体系（后端 JWT）
- **注册 / 登录**：账号数据接入云端后端（FastAPI），密码以 bcrypt 加盐哈希存储，绝不存明文
- **登录态保持**：JWT token + 用户信息存 DataStore，重启后自动登录
- **游客浏览**：可「先逛逛」不登录；打卡、点赞、评论、发布时会提示登录
- **4 个课堂演示账号**：一键切换身份，用于现场演示多人评论与点赞效果（密码统一 `123456`）
- 退出登录

### 6. 打卡（习惯养成，本地存储）
- 连续打卡天数 / 累计天数 / 最长纪录 / 传习等级（7 级成长体系）
- **月度打卡日历**：点击任意日期可补打卡或取消打卡，自动标记节气日
- 二十四节气点亮进度（环形徽标，已点亮显示该节气主题色）
- 打卡记录列表（含心情备注）、打卡卡片分享

### 7. 民俗地图（地域民俗可视化，投稿已上后端）
- **Canvas 手绘中国地图**：34 个省级轮廓 + 按经纬度投影的民俗打点，点击查看详情
- 省份筛选、节气筛选（含“当前节气”快捷筛选）、关键词搜索
- **民俗投稿**：用户可补充“我家的民俗”，按省份自动定位到地图（UGC 文化共建，存后端共享）

### 8. 我的（设置与数据）
- 账号信息（头像、昵称、签名）、传习等级、八项统计
- 每日提醒：开关 + 时间选择器（AlarmManager + 通知渠道 + 开机自动恢复）
- 主题模式：跟随系统 / 浅色 / 深色
- 数据管理：打卡记录导出为 CSV（复制到剪贴板）、清空本机数据
- **图片来源与授权页**：列出全部配画的作者、年代、材质与藏品编号
- 二十四节气文化小知识、关于本项目

### 9. 神秘文化（云端 AI 趣味体验）
- **今日运势**：选一个生肖（横 6 纵 2 排布），生成当日综合运势 + 四维度星评 + 幸运色/数字 + 宜忌
- **灵签占卜**：64 签（覆盖综合/事业/财运/感情/学业/健康六向），真随机抽取，当天保持、再抽覆盖
- **塔罗牌阵**：标准 78 张牌堆（22 大阿卡纳 + 56 小阿卡纳），抽三张观过去·现在·未来，正逆位解读
- **手相识读**：上传手掌照片，Qwen-VL 多模态识别掌纹特征（手型/手指/感情线/生命线/智慧线/掌纹）
- **面相解读**：上传面部照片，AI 识读五官特征（脸型/眉毛/眼睛/鼻子/嘴唇）
- 隐私说明：照片仅用于本次分析、取得授权后上传、分析后不留存

---

## 三、技术实现

### 1. 技术栈

| 层次 | 技术 |
| --- | --- |
| 语言 / UI | Kotlin 2.2.10 + Jetpack Compose（Material 3） |
| 架构 | 单 Activity + Compose Navigation，Repository 模式，手写轻量 DI 容器 |
| 网络层 | Retrofit 2.11 + OkHttp + Gson（调后端 REST 接口） |
| 后端 | Python + FastAPI + Uvicorn（见 `backend/` 目录，独立部署） |
| 本地存储 | Room 2.8.5（打卡/收藏/用户镜像）+ DataStore Preferences（设置/会话） |
| 云端存储 | PostgreSQL 16（Docker 容器，用户表 + 占卜记录 + 社交动态/评论/点赞/投稿） |
| 认证 | JWT + bcrypt（后端签发，前端 DataStore 保存 token） |
| 多模态 | 通义千问 Qwen-VL（qwen3.8-flash，DashScope API，手相/面相识别） |
| 异步 | Kotlin Coroutines + Flow，`collectAsStateWithLifecycle` |
| 绘图 | Compose `Canvas`（节气罗盘、中国地图、月历网格），零图片资源 |
| 通知 | AlarmManager + NotificationCompat + BroadcastReceiver（含开机恢复） |
| 测试 | JUnit4 单元测试（天文算法、打卡统计） |

### 2. 节气天文算法（本项目技术亮点）

节气本质是**太阳在黄道上的视位置每运行 15° 所对应的时刻**。项目没有使用“节气日期表”，
而是实时求解天文方程：

1. 用 **VSOP87D** 地球日心黄经的截断周期项级数（黄经 65 项 + 黄经时间项、黄纬 7 项、距离 50 项）
   计算给定儒略日的地球日心黄经；
2. 换算为太阳地心视黄经，并做**光行差**与**章动**修正；
3. 以平气法给出的常年平均日期为初值，用**牛顿迭代**求解「太阳视黄经 = 目标黄经」的时刻，
   并用二分法兜底保证鲁棒性。

关键实现要点（踩过的坑，值得写进报告）：

- VSOP87D 系数表的振幅 A 以 **1e-8 弧度**为单位，必须归一化后再求和，
  否则常数项 175347046 会被当成 1.75 亿弧度；
- 儒略日换算的基准是**该日 0 时 UT**，J2000.0 历元（2000-01-01 12:00 UT）= JD 2451545.0；
- 牛顿迭代的步长直接用角度残差除以太阳日均行度 0.9856473°/天 即可，收敛极快。

**精度实测**（单元测试，2024 年 24 个节气对比紫金山天文台公布值）：

| 指标 | 结果 |
| --- | --- |
| 公历日期正确率 | **24/24（100%）** |
| 精确时刻最大偏差 | 约 29 分钟（截断级数 + 未做 TT−UT 修正所致） |

> 由于节气间隔约 15.2 天，该精度足以保证用户看到的日期永远正确，
> 也无需为 1900—2100 年维护任何日期表。

### 3. 数据库设计（Room，本地）

> 迁移后：**打卡（check_in）与收藏（favorite）保留在本地 Room**；
> 账号、动态、评论、点赞、民俗投稿已上后端 PostgreSQL（见 3.1）。
> Room 中的 `post`/`comment`/`like_record`/`folk_custom(用户投稿)` 表已不再由前端读写。

```
check_in（打卡表）
  date TEXT PRIMARY KEY   -- yyyy-MM-dd，天然去重，一天一条
  termIndex INTEGER       -- 打卡时所处节气序号 1..24
  termName TEXT           -- 冗余节气名，便于直接展示
  note TEXT               -- 心情备注
  createdAt INTEGER

favorite（收藏表）
  key TEXT PRIMARY KEY    -- term:立春 / custom:12
  type TEXT               -- term / custom
  title, subtitle TEXT
  refId INTEGER
  createdAt INTEGER

folk_custom（民俗表，仅存内置数据）
  id INTEGER PRIMARY KEY AUTOINCREMENT
  title, description TEXT
  termName TEXT           -- 关联节气，可空表示全年民俗
  province, city TEXT
  latitude, longitude REAL-- 用于地图打点
  isBuiltIn INTEGER       -- 内置数据（用户投稿改存后端）
  createdAt INTEGER
```

### 3.1 后端数据库设计（PostgreSQL，云端）

账号、神秘文化、以及社交数据（动态/评论/点赞/民俗投稿）存云端 PostgreSQL
（`backend/app/models/entities.py`）：

```
users（用户表）
  id SERIAL PRIMARY KEY
  username TEXT UNIQUE     -- 登录名
  nickname TEXT            -- 昵称
  password_hash TEXT       -- bcrypt 哈希
  bio TEXT                 -- 个性签名
  avatar_color INTEGER     -- 头像颜色
  is_demo BOOLEAN          -- 是否演示账号
  created_at TIMESTAMP

fortune_records（占卜/运势记录表）
  id SERIAL PRIMARY KEY
  username TEXT            -- 归属用户（未登录为 guest）
  kind TEXT                -- sign / tarot / fortune / palm / face
  seed_key TEXT            -- 唯一键：{username}|{kind}
  result TEXT              -- JSON 结果字符串
  created_at TIMESTAMP

posts（打卡广场动态表）
  id SERIAL PRIMARY KEY
  author TEXT              -- 作者 username
  author_name / author_color TEXT / INTEGER
  term_index / term_name INTEGER / TEXT
  content TEXT
  image_asset / mood TEXT
  like_count / comment_count INTEGER   -- 冗余计数
  created_at_ms BIGINT     -- 毫秒时间戳

comments（评论表，服务节气/民俗/动态三场景）
  id SERIAL PRIMARY KEY
  target_type TEXT         -- term / folk / post
  target_id INTEGER        -- 目标 id
  parent_id INTEGER        -- 0 为顶级评论
  author / author_name / author_color ...
  content TEXT
  like_count INTEGER
  created_at_ms BIGINT

like_records（点赞表，target_type+target_id+username 唯一）
  id SERIAL PRIMARY KEY
  target_type / target_id TEXT / INTEGER
  username TEXT
  created_at_ms BIGINT

folk_customs（民俗投稿表）
  id SERIAL PRIMARY KEY
  title / term_name / province / city / description TEXT
  latitude / longitude DOUBLE PRECISION
  contributor TEXT         -- 投稿者 username
  contributor_name TEXT
  created_at_ms BIGINT
```

### 4. 工程结构

```
app/src/main/java/com/example/jieqi/
├── JieQiApp.kt                    应用入口，持有全局容器
├── MainActivity.kt                单 Activity + Compose 导航
├── data/
│   ├── AppContainer.kt            轻量依赖容器（手写 DI）
│   ├── SettingsStore.kt           DataStore 设置存储
│   ├── SessionStore.kt            DataStore 会话存储（token + 用户 id）
│   ├── Repositories.kt            打卡 / 收藏仓库
│   ├── UserRepository.kt          用户仓库（走后端注册/登录 + 本地镜像）
│   ├── FolkCustomRepository.kt    民俗仓库（含内置数据播种）
│   ├── content/
│   │   ├── SolarTermContent.kt    24 节气完整内容库（三候/习俗/食俗/养生/诗话）
│   │   ├── FolkCustomContent.kt   34 条各地民俗内置数据
│   │   └── DailyContentFactory.kt 每日一句/一俗/一食、分享文案生成
│   ├── remote/                    网络层（调后端 FastAPI）
│   │   ├── Models.kt              API 数据模型（Auth/Fortune/Sign/Tarot/Vision）
│   │   ├── JieQiApi.kt            Retrofit 接口定义
│   │   ├── ApiClient.kt           Retrofit 单例（BASE_URL 指向后端）
│   │   └── MysticRepository.kt    神秘文化仓库（运势/占卜/手相/面相）
│   └── db/                        Room 实体、DAO、数据库
├── domain/
│   ├── SolarTermCalculator.kt     VSOP87D 天文算法（核心）
│   ├── TermDateUtils.kt           当前节气 / 倒计时 / 年度节气表（带缓存）
│   └── CheckInCalculator.kt       连续天数 / 等级 / 月历排版（纯函数，可测）
├── notify/                        AlarmManager 调度 + 通知 + 开机恢复
├── ui/
│   ├── theme/                     配色（24 节气主题色）、字体、Material3 主题
│   ├── components/                通用组件、节气罗盘、中国地图画布
│   ├── navigation/                路由表与底部导航
│   └── screens/                   首页 / 节气 / 详情 / 打卡 / 地图 / 我的 / 神秘文化
└── util/ShareUtils.kt             系统分享

app/src/test/java/com/example/jieqi/domain/
├── SolarTermCalculatorTest.kt     天文算法精度测试（含 2024 全年公布值比对）
└── CheckInCalculatorTest.kt       打卡统计逻辑测试

backend/                           Python + FastAPI 后端（独立部署到云服务器）
├── app/
│   ├── main.py                    FastAPI 入口（lifespan 自动建表 + 播种演示账号）
│   ├── core/                      config / database / security（JWT + bcrypt）
│   ├── models/entities.py         User + FortuneRecord ORM 模型
│   ├── routers/                   auth / fortune / vision 路由
│   ├── services/                  fortune / divination / vision / seed 服务
│   └── data/                      运势 / 占卜 / 手相面相语料库
├── requirements.txt               dashscope / fastapi / sqlalchemy / asyncpg / bcrypt
├── Dockerfile
└── docker-compose.yml             backend + postgres 两容器编排
```

### 5. 涉众分析与功能映射
| 涉众 | 需求 | 对应功能 |
| --- | --- | --- |
| 青少年 / 大学生（核心用户） | 碎片化学习、互动与分享 | 每日一俗、打卡成长体系、节气罗盘、一键分享 |
| 家长 | 亲子传统文化教育 | 节气诗话、三候物候、顺时养生等易读内容 |
| 教师 | 班会 / 校本素材 | 节气详情页结构化内容、可直接分享的图文文案 |
| 文化机构 / 非遗传承人 | 权威内容供给 | 内置权威民俗条目 + 投稿入口，支持后续接入后台 |
| 宣传 / 文旅部门 | 文化宣传载体 | 地域民俗地图、可传播的分享卡片 |

---

## 四、测试

```bash
gradle :app:testDebugUnitTest
```

测试报告：`app/build/reports/tests/testDebugUnitTest/index.html`

覆盖内容（共 23 个单元用例 + 3 个仪器化用例，全部通过）：

- **天文算法**：2024 年 24 个节气与紫金山天文台公布值逐一比对（日期 100% 一致、时刻误差 ≤ 45 分钟）；
  春分/秋分时太阳视黄经为 0°/180°；儒略日换算可逆；1900—2100 抽样年份节气顺序与跨年正确。
- **节气日期工具**：当前/下一节气判定、跨年边界（1 月与 12 月底）、
  节气当天在精确时刻前后归属哪个节气、倒计时文案。
- **打卡逻辑**：连续天数（含“今天未打卡但昨天打过不清零”规则）、最长纪录、等级门槛、
  月度日历排版与本月统计。
- **真机冒烟**（`gradle :app:connectedDebugAndroidTest`，需连接设备）：首页渲染、底部导航切换、一键打卡。

---

## 五、常见问题

**Q0：Gradle 下载依赖特别慢 / Sync 卡在 Downloading？**

分两种情况，先确认卡在哪一步：

**情况一：卡在 `Downloading https://services.gradle.org/distributions/gradle-x.x.x-bin.zip`**
这是**下载 Gradle 本体**（约 130 MB），不是依赖。该地址会 307 跳转到 GitHub，国内极易超时。
本工程已通过 `gradle/wrapper/gradle-wrapper.properties` 指定腾讯云镜像+官方 SHA-256，实测：

| 下载源 | 实测速度 |
| --- | --- |
| `services.gradle.org`（会跳 GitHub） | 连接超时 |
| 腾讯云镜像（本工程默认，已校验与官方一致） | **43 MB/s** |

**情况二：卡在下载依赖（`Downloading https://...aar/jar`）**
工程已在 `settings.gradle.kts` 中预置**阿里云镜像**，实测同一台机器上：

| 配置 | 冷构建耗时 |
| --- | --- |
| 官方仓库（`repo1.maven.org`） | 252 秒 |
| 阿里云镜像（本工程默认） | 129 秒 |

说明：`dl.google.com`（AndroidX / Compose）在国内实测约 7 MB/s，本身并不慢；
真正的瓶颈是 **Maven Central 只有约 0.5 MB/s**，Kotlin、Coroutines、KSP 等依赖都走那里，
镜像替换的正是这一环。**首次 Sync 约 2 分钟，之后不再需要联网**（依赖已进本地缓存）：

| 场景 | 实测耗时 |
| --- | --- |
| 首次 Sync / 冷构建 | 约 2 分钟 |
| 改代码后重新编译 | 约 16 秒 |
| 无改动构建 | 约 1 秒 |

如果 Sync 仍然很慢，可以：
1. `File → Invalidate Caches / Restart` 后重新 Sync；
2. 若本机有代理，可在 `gradle.properties` 中配置：
   ```properties
   systemProp.http.proxyHost=127.0.0.1
   systemProp.http.proxyPort=7890
   systemProp.https.proxyHost=127.0.0.1
   systemProp.https.proxyPort=7890
   ```
   （端口按代理软件实际配置填写；**提交作业前请删除**，否则老师机器上没有代理会构建失败。）
3. 海外网络环境下，把 `settings.gradle.kts` 中的 `maven.aliyun.com` 与
   `gradle-wrapper.properties` 中的腾讯云地址换回官方源即可。

**Q1：Sync 报 “Cannot add extension with name 'kotlin'”？**
AGP 9.x 已内置 Kotlin 支持，工程中**不能**再手动 `apply` `org.jetbrains.kotlin.android`。
本工程的 `app/build.gradle.kts` 只声明 `com.android.application`、`com.google.devtools.ksp`
与 `org.jetbrains.kotlin.plugin.compose`。

**Q2：报 “Dependency requires compileSdk 37”？**
本工程已把所有 androidx 与 Coil 依赖锁定在兼容 compileSdk 36 的版本
（Compose BOM 2026.06.01、Room 2.8.5、**Coil 3.4.0** 等）。
其中 Coil 需要特别注意：3.6.x 要求 compileSdk 37、3.5.0 要求 36，只有 **3.4.0 及以下**能用；
同时 Coil 3.x 会传递引入 Compose 1.12.0 与 kotlin-stdlib 2.4.10，
因此 `app/build.gradle.kts` 中用 `resolutionStrategy.force` 把它们压回兼容版本（详见该文件注释）。
若要升级依赖，请同步升级本机 SDK 平台与 Kotlin 版本。

**Q3：提醒不生效？**Android 13+ 需要通知权限（App 首次启动会申请）；另外部分国产 ROM 需要在系统设置中
允许“自启动/后台运行”。提醒使用 `setInexactRepeating`，触发时间可能有几分钟浮动，属正常现象。

**Q4：节气时刻与官方公布差了几十分钟？**
见上文「节气天文算法」精度说明：日期 100% 正确，时刻因截断级数与未做 TT−UT 修正存在约
半小时偏差。如需秒级精度，可扩充 VSOP87 项数并加入 ΔT 模型。

---

## 六、数据与素材说明

- 24 节气三候、习俗、食俗、养生、诗话均为公开传统民俗资料整理，用于课程教学演示。
- **节气配画共 30 幅，全部来自大都会艺术博物馆（The Metropolitan Museum of Art）Open Access 开放数据，
  采用 CC0 1.0 公共领域贡献许可**：可自由复制、修改与分发，无署名义务。
  项目仍在「我的 → 图片来源与授权」页完整标注了每幅画的作者、年代与藏品编号。
- 配图选取的是与中国节气意象契合的中国/日本绘画，例如金农《梅花图》、赵孟坚《梅花竹石图咏》、
  王冕《断桥暗香》、沈周《秋林静钓》、王翚《溪山雨后》、伊藤若冲《月下白梅》、歌川广重《满月飞雁》等。
- 民俗条目的经纬度为城市中心近似坐标，用于地图打点示意。
- 中国地图为**风格化手绘轮廓**（`ui/components/FolkMapCanvas.kt` 中以归一化坐标定义），
  用于数据分布示意，不作为测绘用途，因此工程内不含任何地图 SDK 与 GeoJSON 资源，完全离线可运行。
- 除 AndroidX 官方库与 Coil（图片加载）外，前端新增 Retrofit/OkHttp/Gson（网络请求，调后端）。
- 后端提供账号、占卜、多模态识别与社交服务，需联网访问（后端地址见 `data/remote/Models.kt` 的 `ApiConfig.BASE_URL`）。
- **离线可用**：节气内容浏览、打卡、收藏、节气罗盘、民俗地图（内置数据）可完全离线运行；
  **需联网**：账号登录、神秘文化板块、打卡广场动态/评论/点赞、民俗投稿。

---

## 七、后端部署说明（可选，课程演示可不部署）

后端独立于 App，是一个 FastAPI 服务，部署在云服务器 Docker 容器中，通过 HTTP 接口供 App 调用。

### 1. 后端接口

| 接口 | 方法 | 说明 |
| --- | --- | --- |
| `/api/auth/register` | POST | 注册（返回 JWT + 用户信息） |
| `/api/auth/login` | POST | 登录（OAuth2 表单） |
| `/api/auth/me` | GET | 获取当前用户 |
| `/api/fortune` | GET | 今日运势（`?zodiac=龙`） |
| `/api/divination/sign` | GET | 抽灵签（`?username=xxx`） |
| `/api/divination/sign/today` | GET | 当天已抽灵签 |
| `/api/divination/tarot` | GET | 抽塔罗（`?username=xxx&count=3`） |
| `/api/divination/tarot/today` | GET | 当天已抽塔罗 |
| `/api/palm/analyze` | POST | 手相识别（multipart 上传图片） |
| `/api/face/analyze` | POST | 面相识别（multipart 上传图片） |
| `/api/social/posts` | GET/POST | 动态列表 / 发布动态 |
| `/api/social/posts/{id}` | DELETE | 删除动态 |
| `/api/social/comments` | GET/POST | 评论列表 / 发表评论 |
| `/api/social/comments/count` | GET | 评论数 |
| `/api/social/comments/{id}` | DELETE | 删除评论 |
| `/api/social/like` | GET/POST | 查询 / 切换点赞 |
| `/api/social/member_count` | GET | 社区成员数 |
| `/api/social/folk_customs` | GET/POST | 民俗投稿列表 / 投稿 |
| `/api/social/folk_customs/{id}` | DELETE | 删除投稿 |

### 2. 部署步骤

```bash
# 服务器上（需 Docker + Docker Compose）
cd backend
docker compose up -d --build
```

- 环境变量见 `backend/.env`（数据库密码、SECRET_KEY、DASHSCOPE_API_KEY）
- 数据库：PostgreSQL 16 容器，数据持久化到 Docker volume `pgdata`
- 多模态识别需在阿里云百炼开通 `qwen3.8-flash` 模型并配置 `DASHSCOPE_API_KEY`

### 3. 前端如何切换后端地址

修改 `app/src/main/java/com/example/jieqi/data/remote/Models.kt` 中的：

```kotlin
object ApiConfig {
    const val BASE_URL = "http://你的服务器:8001/"
}
```

> 由于后端使用明文 HTTP，工程已通过 `res/xml/network_security_config.xml` 放行。
