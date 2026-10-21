# M2 节气打卡与习惯养成

> 负责人：成员 2  
> 主题色：朱砂  
> 本文件夹全部**复制自总工程**，原工程未做任何改动。

---

## 一、你负责什么

- F2.1 一键打卡（含心情备注）
- F2.2 月度打卡日历（补打卡 / 取消）
- F2.3 连续打卡与成长等级（7 级）
- F2.4 二十四节气点亮进度
- F2.5 打卡卡片分享

---

## 二、本人负责的文件（在「01_本人负责」里）

| 文件（相对 `com/example/jieqi/`） | 作用 |
|---|---|
| `domain\CheckInCalculator.kt` | 连续天数、最长纪录、等级、月历排版（纯函数） |
| `ui\screens\CheckInScreen.kt` | 打卡页：统计、月历、记录管理 |
| `domain\CheckInCalculatorTest.kt`（测试） | 打卡统计逻辑测试（8 个用例） |

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

## 四、怎么用

## 四、怎么用

1. 用「02_公共依赖」当作基础工程（含 app/、构建脚本、gradle wrapper、res/）
2. 把「01_本人负责/app/」覆盖进去
3. 从 M1 的「03_图片资源」拷一份 assets/images 进来（否则古画不显示）
4. 用 Android Studio 打开，Sync 后即可编译
5. 只改「01_本人负责」里对应的文件；需要动公共文件时按上面的约定走

## 五、验收标准

- 你负责的页面能正常打开、功能可用
- `gradlew.bat :app:assembleDebug` 编译通过
- `gradlew.bat :app:lintDebug` 无错误
- `gradlew.bat :app:testDebugUnitTest` 全绿
