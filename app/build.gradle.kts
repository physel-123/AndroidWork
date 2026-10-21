plugins {
    id("com.android.application")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.10"
}

android {
    namespace = "com.example.jieqi"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.jieqi"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
        vectorDrawables { useSupportLibrary = true }
    }

    buildTypes {
        debug { isMinifyEnabled = false }
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

// 依赖版本统一锁定。
//
// 背景：Coil 3.6.x 会传递引入 Compose 1.12.0 / kotlin-stdlib 2.4.10，
// 前者要求 compileSdk 37（本机只装了 android-36），后者要求 Kotlin 2.4 编译器（本工程为 2.2.10）。
// 这里用 resolutionStrategy 把这两个传递依赖压回与 compileSdk 36 / Kotlin 2.2.10 兼容的版本。
configurations.configureEach {
    resolutionStrategy {
        force(
            "org.jetbrains.kotlin:kotlin-stdlib:2.2.10",
            "androidx.compose.animation:animation:1.11.4",
            "androidx.compose.animation:animation-core:1.11.4",
            "androidx.compose.foundation:foundation:1.11.4",
            "androidx.compose.foundation:foundation-layout:1.11.4",
            "androidx.compose.runtime:runtime:1.11.4",
            "androidx.compose.ui:ui:1.11.4",
            "androidx.compose.ui:ui-graphics:1.11.4",
            "androidx.compose.ui:ui-text:1.11.4",
            "androidx.compose.ui:ui-unit:1.11.4",
            "androidx.compose.ui:ui-util:1.11.4",
            "androidx.compose.ui:ui-geometry:1.11.4"
        )
    }
}

dependencies {
    // Compose BOM 统一管理 Compose 各库版本（2026.06.01 → Compose 1.11.4 / Material3 1.4.0）
    // 这些版本均兼容本机已安装的 android-36 平台，无需额外下载 SDK
    val composeBom = platform("androidx.compose:compose-bom:2026.06.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.activity:activity-compose:1.12.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("androidx.navigation:navigation-compose:2.9.8")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.room:room-runtime:2.8.5")
    implementation("androidx.room:room-ktx:2.8.5")
    ksp("androidx.room:room-compiler:2.8.5")

    implementation("androidx.datastore:datastore-preferences:1.2.1")

    // 图片 / 动图 / 视频首帧加载（统一由 Coil 处理，静态图与 GIF 都能显示）
    // 版本固定 3.4.0：3.6.x / 3.5.0 分别要求 compileSdk 37 / 36，本工程使用 android-36 平台
    implementation("io.coil-kt.coil3:coil-compose:3.4.0")
    implementation("io.coil-kt.coil3:coil-gif:3.4.0")
    implementation("io.coil-kt.coil3:coil-video:3.4.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.4.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test:rules:1.7.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
