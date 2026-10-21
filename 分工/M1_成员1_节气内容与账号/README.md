# M1 节气内容展示与检索 + S0 注册 / 登录

> 负责人：成员 1（组长）  
> 主题色：竹青  
> 本文件夹全部**复制自总工程**，原工程未做任何改动。

---

## 一、你负责什么

- F1.1 当日节气智能推荐
- F1.2 节气日期天文计算与倒计时
- F1.3 二十四节气图谱检索
- F1.4 节气文化档案（三候 / 习俗 / 食俗 / 养生 / 诗词）
- F1.5 古画配图浏览与全屏查看
- S0.1 注册（账号唯一校验）
- S0.2 登录（加盐 SHA-256 摘要）
- S0.3 登录态持久化
- S0.4 游客浏览模式
- S0.5 演示账号一键切换

---

## 二、本人负责的文件（在「01_本人负责」里）

| 文件（相对 `com/example/jieqi/`） | 作用 |
|---|---|
| `domain\SolarTermCalculator.kt` | VSOP87D 天文算法：节气时刻求解、儒略日换算 |
| `domain\TermDateUtils.kt` | 当前 / 下一节气、倒计时、年度节气表（带缓存） |
| `data\content\SolarTermContent.kt` | 24 节气完整内容库（三候/习俗/食俗/养生/诗话） |
| `data\content\ArtworkLibrary.kt` | 30 幅公共领域古画配图库（含作者与馆藏） |
| `data\content\DailyContentFactory.kt` | 每日一句 / 一俗 / 一食、分享文案生成 |
| `data\UserRepository.kt` | 注册、登录、演示账号、会话保持 |
| `data\SessionStore.kt` | 登录态存储（DataStore） |
| `ui\screens\HomeScreen.kt` | 首页：当前节气、倒计时、今日一俗、足迹 |
| `ui\screens\TermListScreen.kt` | 节气列表：罗盘、四季筛选、搜索 |
| `ui\screens\TermDetailScreen.kt` | 节气详情：文化档案 + 古画 + 讨论区 |
| `ui\screens\LoginScreen.kt` | 登录 / 注册页（含演示账号一键登录） |
| `ui\screens\ImageCreditsScreen.kt` | 配图来源与授权页 |
| `ui\components\TermWheel.kt` | 节气罗盘（Canvas 自绘、点击命中） |
| `ui\components\ArtworkViews.kt` | 古画展示、全屏查看组件 |
| `domain\SolarTermCalculatorTest.kt`（测试） | 天文算法精度测试（对比紫金山天文台公布值） |
| `domain\TermDateUtilsTest.kt`（测试） | 节气日期工具测试（跨年、时刻边界） |

路径已保留完整层级，直接把「01_本人负责/app/」整个覆盖到你工程的 app/ 即可。

## 三、公共依赖（在「02_公共依赖」里）

这些文件**不属于你一个人**，但你的代码需要它们才能编译：

| 文件 | 说明 |
|---|---|
| `MainActivity.kt` | 唯一的 Activity，承载 Compose 导航与登录态判断 |
| `JieQiApp.kt` | Application，配置 Coil 图片加载器 |
| `data\AppContainer.kt` | 依赖容器：要在里面注册自己的 Repository |
| `data\SettingsStore.kt` | DataStore：提醒设置、昵称、主题 |
| `data\Repositories.kt` | 打卡仓库 + 收藏仓库（M2 与 M4 共用） |
| `data\db\Entities.kt` | 基础表实体：打卡 / 收藏 / 民俗 |
| `data\db\Daos.kt` | 基础表 DAO |
| `data\db\JieQiDatabase.kt` | 数据库定义：新增表时要在这里登记 |
| `ui\LocalProviders.kt` | CompositionLocal：依赖容器与 Snackbar |
| `ui\navigation\Navigation.kt` | 路由表：新增页面时要在这里加路由 |
| `ui\theme\Color.kt` | 国风配色与 24 节气主题色 |
| `ui\theme\Type.kt` | 字体排版 |
| `ui\theme\Theme.kt` | Material 3 主题 |
| `ui\components\Components.kt` | 通用组件：卡片 / 标签 / 进度条 / 统计格 |
| `util\ImageUtils.kt` | 图片加载、头像、相对时间 |
| `util\ShareUtils.kt` | 系统分享 |
| `AndroidManifest.xml` | 应用清单：权限 / Activity / 广播接收器 |
| `res\` | 资源目录：图标、颜色、字符串、主题（含 values-night） |
| `app\src\androidTest\...\MainActivitySmokeTest.kt` | 整机冒烟测试（跨模块的集成测试） |

另外还包含：`settings.gradle.kts`、`build.gradle.kts`、`gradle.properties`、
`gradlew` / `gradlew.bat`、`gradle/wrapper/`、`app/build.gradle.kts`、`app/proguard-rules.pro`。

> **约定**：「02_公共依赖」里的文件以组长维护的版本为准。
> 当你需要新增 Repository、新增路由、新增数据库表时，
> 先把改动发给组长合并，再同步回自己的工程，**不要各自维护一份**，否则最后无法合并。

## 四、图片资源（在「03_图片资源」里）

30 幅公共领域古画（约 3.7 MB，来自大都会艺术博物馆 Open Access，CC0）：

```
03_图片资源/app/src/main/assets/images/*.jpg
```

这些图片由 M1 的 `ArtworkLibrary.kt` 引用，
**其他成员的工作区也需要这份资源**，否则图片显示不出来。

## 五、怎么用

## 四、怎么用

1. 用「02_公共依赖」当作基础工程（含 app/、构建脚本、gradle wrapper、res/）
2. 把「01_本人负责/app/」覆盖进去
3. 把「03_图片资源/app/」覆盖进去（古画配图）
4. 用 Android Studio 打开，Sync 后即可编译
5. 只改「01_本人负责」里对应的文件；需要动公共文件时按上面的约定走

## 五、验收标准

- 你负责的页面能正常打开、功能可用
- `gradlew.bat :app:assembleDebug` 编译通过
- `gradlew.bat :app:lintDebug` 无错误
- `gradlew.bat :app:testDebugUnitTest` 全绿
