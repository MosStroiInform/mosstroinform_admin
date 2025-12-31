import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    jvm()
    
    js {
        browser()
        binaries.executable()
    }
    
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.material3.windowsize)
            implementation(libs.ktor.client.okhttp)
        }
        
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        
        jsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
        
        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
        
        commonMain.dependencies {
            // Compose
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            
            // Coroutines
            implementation(libs.kotlinx.coroutines.core)
            
            // Kotlinx
            implementation(libs.kotlinx.serialization.json)
            // Фиксируем версию 0.6.1, чтобы избежать конфликтов с 0.7.1 (где Instant в kotlin.time)
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
            
            // Ktor
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.contentnegotiation)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.client.websockets)
            implementation(libs.ktor.serialization.json)
            
            // Koin
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            
            // Navigation (Official JetBrains)
            implementation(libs.navigation.compose)
        }
        
        // DataStore только для поддерживаемых платформ (Android, iOS, JVM)
        androidMain.dependencies {
            implementation(libs.datastore.preferences.android)
        }
        
        iosMain.dependencies {
            implementation(libs.datastore.preferences)
        }
        
        jvmMain.dependencies {
            implementation(libs.datastore.preferences)
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.ktor.client.cio)
            implementation(libs.androidx.savedstate)
            // kotlinx.datetime уже в commonMain, но явно указываем для JVM runtime
            // Фиксируем версию 0.6.1, чтобы избежать конфликтов с 0.7.1
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
        }
        
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.vasmarfas.mosstroiinformadmin"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.vasmarfas.mosstroiinformadmin"
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
            isMinifyEnabled = false
            isDebuggable = false
        }
        getByName("debug") {
            isDebuggable = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "com.vasmarfas.mosstroiinformadmin.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "MosStroiInform Admin"
            packageVersion = "1.0.0"
            
            description = "Административная панель MosStroiInform"
            vendor = "vasmarfas"

            windows {
                menuGroup = "MosStroiInform"
                shortcut = true
            }
            
            macOS {
                bundleID = "com.vasmarfas.mosstroiinformadmin"
                packageVersion = "1.0.0"
            }
            
            linux {
                packageName = "mosstroiinform-admin"
                menuGroup = "Office"
                appCategory = "Office"
            }
        }
        
        // Отключаем ProGuard для release сборки
        buildTypes {
            release {
                proguard {
                    isEnabled.set(false)
                }
            }
        }
    }
}

// Явно добавляем kotlinx-datetime в runtime classpath для jvmRun
// Это критично, так как Compose Desktop не всегда автоматически включает зависимости
// Используем resolutionStrategy для принудительного использования версии 0.6.1
configurations.all {
    resolutionStrategy {
        force("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
    }
}


