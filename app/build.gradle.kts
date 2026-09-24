import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// Release signing comes from HARBOR_* environment variables, else the first properties file found:
// $HARBOR_SIGNING_PROPERTIES, keystore.properties at the repo root (gitignored), then the cloud project store.
// Keys: storeFile, storePassword, keyAlias, keyPassword. Without them, release builds come out unsigned.
val signingProps = Properties().apply {
    listOfNotNull(
        System.getenv("HARBOR_SIGNING_PROPERTIES"),
        rootProject.file("keystore.properties").path,
        "/cursor/stores/bc-fab66b0a-5b06-4520-b355-67c16b998019/artifacts/signing-notes.txt",
    ).map(::File).firstOrNull { it.isFile }?.reader()?.use { load(it) }
}

fun signingValue(env: String, key: String): String? = System.getenv(env) ?: signingProps.getProperty(key)

val releaseKeystore: File? = signingValue("HARBOR_KEYSTORE", "storeFile")
    ?.let { rootProject.file(it) }
    ?.takeIf { it.isFile }

android {
    namespace = "com.harborreel.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.harborreel.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 20
        versionName = "1.19"
    }

    signingConfigs {
        if (releaseKeystore != null) {
            create("release") {
                storeFile = releaseKeystore
                storePassword = signingValue("HARBOR_KEYSTORE_PASSWORD", "storePassword")
                keyAlias = signingValue("HARBOR_KEY_ALIAS", "keyAlias")
                keyPassword = signingValue("HARBOR_KEY_PASSWORD", "keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.findByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

val driveApkDir = "G:/My Drive/HarborReel"

tasks.register<Copy>("copyDebugApkToDrive") {
    dependsOn("assembleDebug")
    from(layout.buildDirectory.file("outputs/apk/debug/app-debug.apk"))
    into(driveApkDir)
    rename { "HarborSlots-debug.apk" }
}

tasks.register<Copy>("copyReleaseApkToDrive") {
    dependsOn("assembleRelease")
    from(layout.buildDirectory.file("outputs/apk/release/app-release.apk"))
    into(driveApkDir)
    rename { "HarborSlots-release.apk" }
}

tasks.matching { it.name == "assembleDebug" }.configureEach {
    finalizedBy("copyDebugApkToDrive")
}

tasks.matching { it.name == "assembleRelease" }.configureEach {
    finalizedBy("copyReleaseApkToDrive")
}

dependencies {
    implementation(project(":engine"))
    implementation(platform(libs.compose.bom))
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.navigation.compose)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.icons)
    debugImplementation(libs.compose.ui.tooling)
}
