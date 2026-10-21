// ─────────────────────────────────────────────────────────────────────────────
// 仓库配置说明（国内网络优化）
//
// 实测（同一台机器）：
//   dl.google.com          ≈ 7.4 MB/s   —— Google 的 AndroidX / Compose 包本身就很快
//   repo1.maven.org        ≈ 0.5 MB/s   —— Kotlin、Coroutines、KSP 等走了这里，是真正的瓶颈
//   maven.aliyun.com/public ≈ 1.8 MB/s  —— 比 Maven Central 快约 3.6 倍
//
// 因此这里保留 Google 官方源（快且权威），把「Maven Central」替换为阿里云聚合镜像。
// 若在海外网络环境下构建，把 maven.aliyun.com 的两行注释掉即可恢复官方源。
// ─────────────────────────────────────────────────────────────────────────────
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        // 国内镜像：加速 Kotlin / KSP 等插件的下载
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        // 国内镜像：加速 Kotlin / Coroutines / JUnit 等依赖的下载
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        mavenCentral()
    }
}

rootProject.name = "JieQi"
include(":app")
