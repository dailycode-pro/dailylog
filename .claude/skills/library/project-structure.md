# Project Structure Guide

## Kotlin Multiplatform Library

### Root `settings.gradle.kts`

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "my-library"

include(":my-library-core")
include(":my-library-compose")
include(":sample:android")
include(":sample:desktop")
```

### Root `build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.maven.publish) apply false
    alias(libs.plugins.binary.compatibility.validator)
}

apiValidation {
    // Exclude non-published modules
    ignoredProjects.addAll(listOf("sample"))
}
```

### Core Module `build.gradle.kts` (KMP)

```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.maven.publish)
}

kotlin {
    explicitApiMode() // 🔑 Forces public/internal visibility annotations
    
    // Targets
    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_11)
                }
            }
        }
        publishLibraryVariants("release")
    }
    
    jvm()
    
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    
    // Apply iOS hierarchy
    applyDefaultHierarchyTemplate()
    
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
        androidMain.dependencies {
            // Android-specific deps
        }
    }
}

android {
    namespace = "com.example.mylibrary.core"
    compileSdk = 35
    defaultConfig {
        minSdk = 21
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
```

### Compose Module `build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.maven.publish)
}

kotlin {
    explicitApiMode()
    
    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_11)
                }
            }
        }
        publishLibraryVariants("release")
    }
    
    jvm()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    
    applyDefaultHierarchyTemplate()
    
    sourceSets {
        commonMain.dependencies {
            implementation(project(":my-library-core"))
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.animation)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.uiTest)
        }
    }
}

android {
    namespace = "com.example.mylibrary.compose"
    compileSdk = 35
    defaultConfig {
        minSdk = 21
    }
}
```

---

## Version Catalog Template

### `gradle/libs.versions.toml`

```toml
[versions]
# ⚠️ ALWAYS web-search for latest stable versions before using!
kotlin = "2.1.0"
agp = "8.7.3"
compose-multiplatform = "1.7.3"
coroutines = "1.10.1"
binary-compatibility-validator = "0.17.0"
maven-publish = "0.30.0"
kover = "0.9.1"

[libraries]
kotlinx-coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "coroutines" }
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "coroutines" }

[plugins]
kotlin-multiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
android-library = { id = "com.android.library", version.ref = "agp" }
compose-multiplatform = { id = "org.jetbrains.compose", version.ref = "compose-multiplatform" }
compose-compiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
maven-publish = { id = "com.vanniktech.maven.publish", version.ref = "maven-publish" }
binary-compatibility-validator = { id = "org.jetbrains.kotlinx.binary-compatibility-validator", version.ref = "binary-compatibility-validator" }
kover = { id = "org.jetbrains.kotlinx.kover", version.ref = "kover" }
```

---

## gradle.properties

```properties
# Kotlin
kotlin.code.style=official
kotlin.mpp.androidSourceSetLayoutVersion=2
kotlin.mpp.stability.nowarn=true
kotlin.native.cacheKind=none

# Gradle
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configuration-cache=true

# Android
android.useAndroidX=true
android.nonTransitiveRClass=true
```

---

## Package Structure

```
com.example.mylibrary/
├── core/                      # Core module
│   ├── model/                 # Data classes, sealed types
│   │   ├── TooltipConfig.kt
│   │   └── Position.kt
│   ├── controller/            # Business logic
│   │   ├── TooltipController.kt        # Public API (interface)
│   │   └── TooltipControllerImpl.kt    # Internal implementation
│   ├── util/                  # Internal utilities
│   │   └── MathUtils.kt      # internal visibility
│   └── MyLibrary.kt          # Top-level factory functions
│
├── compose/                   # Compose module
│   ├── components/            # Composable functions
│   │   ├── Tooltip.kt
│   │   └── TooltipHost.kt
│   ├── modifier/              # Modifier extensions
│   │   └── TooltipAnchor.kt
│   ├── state/                 # Compose state holders
│   │   └── TooltipState.kt
│   └── theme/                 # Theming (optional)
│       └── TooltipTheme.kt
```

---

## ProGuard / R8 Rules

For Android consumers, include in your library:

### `consumer-rules.pro` (in the library module)

```proguard
# Keep public API
-keep class com.example.mylibrary.** { public *; }

# Keep data classes for serialization (if needed)
-keepclassmembers class com.example.mylibrary.model.** {
    <fields>;
    <init>(...);
}
```

Place this in `src/main/` of the Android target. It will be automatically bundled with the AAR.

---

## .gitignore

```gitignore
# Gradle
.gradle/
build/
!gradle-wrapper.jar

# IDE
.idea/
*.iml

# OS
.DS_Store
Thumbs.db

# Local config
local.properties
*.jks
*.gpg

# Generated
/api/
```

---

## Key Configuration Decisions

### `explicitApiMode()`

**Always enable for libraries.** Forces you to declare visibility for all public declarations.

```kotlin
kotlin {
    explicitApiMode() // Warnings → errors for missing visibility modifiers
}
```

Without this, everything defaults to `public`, which leads to accidental API exposure.

### Source Set Hierarchy

Use `applyDefaultHierarchyTemplate()` for standard KMP targets. The default hierarchy:

```
common
├── jvm
├── native
│   ├── apple
│   │   ├── ios
│   │   │   ├── iosX64
│   │   │   ├── iosArm64
│   │   │   └── iosSimulatorArm64
│   │   └── macos
│   └── linux
└── js / wasmJs
```

### publishLibraryVariants

For Android, only publish the `release` variant:

```kotlin
androidTarget {
    publishLibraryVariants("release")
}
```
