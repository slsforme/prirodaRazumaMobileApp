import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    kotlin("plugin.serialization") version "1.9.20"
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation("io.ktor:ktor-client-core:2.0.3")
            implementation("io.ktor:ktor-client-content-negotiation:2.0.3")
            implementation("io.ktor:ktor-serialization-kotlinx-json:2.0.3")
            implementation("org.jetbrains.compose.material3:material3:1.6.0")
            implementation("androidx.compose.material:material-icons-extended:1.6.8")
            implementation("co.touchlab:kermit:2.0.4")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
            implementation("io.ktor:ktor-client-cio:2.3.7")
            implementation("io.coil-kt:coil-compose:2.4.0")
            implementation("io.insert-koin:koin-core:3.4.0")
            implementation("androidx.navigation:navigation-compose:2.8.0")
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
            implementation("com.darkrockstudios:mpfilepicker:3.1.0")
            implementation("io.github.onseok:peekaboo-ui:0.5.2")
            implementation("io.github.onseok:peekaboo-image-picker:0.5.2")
        }
    }
}

android {
    namespace = "org.example.priroda_razuma"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.example.priroda_razuma"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.androidx.ui.android)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.camera.view)
    debugImplementation(compose.uiTooling)
}