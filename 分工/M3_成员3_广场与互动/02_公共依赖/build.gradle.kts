// 顶层构建脚本：只声明插件版本，具体应用在 :app 模块中完成。
//
// 版本组合说明（本机环境实测可用）：
//   Gradle 9.4.1（本机已缓存，AGP 9.2.1 要求的最低版本正好是 9.4.1）
//   AGP 9.2.1   自 9.x 起 Android 插件内置 Kotlin 支持，无需再单独应用 kotlin-android
//   KSP 2.3.12  新版 KSP 采用独立版本号，兼容 AGP 9 的内置 Kotlin
plugins {
    id("com.android.application") version "9.2.1" apply false
    id("com.google.devtools.ksp") version "2.3.12" apply false
}
