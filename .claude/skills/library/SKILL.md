---
name: kotlin-library
description: Guide for creating production-grade Kotlin and Kotlin Multiplatform libraries following industry best practices. Use when building reusable Kotlin libraries, KMP modules, Compose UI libraries, or any shared Kotlin code intended for distribution. Covers architecture, API design, implementation, sample app, publishing, and documentation.
---

# Kotlin Library Development Guide

## Overview

Create professional, production-ready Kotlin libraries that developers love to use. A great library is measured by its API clarity, reliability, documentation quality, and ease of integration.

This skill covers:
- Pure Kotlin (JVM) libraries
- Kotlin Multiplatform (KMP) libraries targeting Android, iOS, Desktop, Web
- Jetpack Compose / Compose Multiplatform UI libraries
- Gradle plugins and convention plugins

---

# ⚠️ CRITICAL RULE: TestApp Is Mandatory

**Every library MUST include a runnable TestApp (sample application).**

The TestApp is NOT optional. It is the primary way to verify the library works. Before considering any library "done", the TestApp must:

1. **Exist** — as a separate Gradle module (`testapp/` or `sample/`)
2. **Compile and run** — on at least one target platform
3. **Demonstrate ALL public API** — every public function, class, composable must be showcased
4. **Be self-contained** — a developer clones the repo, opens it, runs the testapp — everything works

### TestApp Structure

**For Android / Compose Multiplatform library:**
```
testapp/
├── build.gradle.kts
└── src/
    └── main/          # or commonMain/ for KMP
        ├── kotlin/
        │   └── com/example/testapp/
        │       ├── MainActivity.kt        # Entry point
        │       └── screens/
        │           ├── MainScreen.kt      # Navigation hub
        │           ├── BasicUsageScreen.kt    # Simple examples
        │           ├── AdvancedUsageScreen.kt # Complex scenarios
        │           └── EdgeCasesScreen.kt     # Boundary conditions
        └── AndroidManifest.xml
```

**For pure Kotlin/JVM library:**
```
testapp/
├── build.gradle.kts
└── src/main/kotlin/
    └── Main.kt    # fun main() with all API demonstrations
```

### TestApp build.gradle.kts

```kotlin
plugins {
    alias(libs.plugins.android.application) // or kotlin("jvm") for JVM
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.example.testapp"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.example.testapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    // ⚠️ Always depend on library via project(), not published artifact
    implementation(project(":my-library-core"))
    implementation(project(":my-library-compose"))
}
```

### TestApp Requirements

Every screen/section in TestApp must:
- Show the **code** conceptually (what the developer writes)
- Show the **result** (what happens at runtime)
- Cover **default behavior** and **custom configuration**
- Include **edge cases**: empty data, long text, rapid interactions, etc.

**For Compose libraries**, each composable gets its own demo section:
```kotlin
@Composable
fun TooltipDemoScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Section 1: Basic usage
        Text("Basic Tooltip", style = MaterialTheme.typography.titleMedium)
        BasicTooltipDemo()

        HorizontalDivider()

        // Section 2: Custom positioning
        Text("Custom Position", style = MaterialTheme.typography.titleMedium)
        PositionTooltipDemo()

        HorizontalDivider()

        // Section 3: Styled tooltip
        Text("Custom Style", style = MaterialTheme.typography.titleMedium)
        StyledTooltipDemo()

        HorizontalDivider()

        // Section 4: Edge cases
        Text("Edge Cases", style = MaterialTheme.typography.titleMedium)
        EdgeCaseTooltipDemo() // very long text, RTL, rapid show/hide
    }
}
```

**For non-Compose libraries**, use `fun main()` with clear output:
```kotlin
fun main() {
    println("=== MyLibrary TestApp ===\n")

    println("--- Basic Usage ---")
    val result = MyLibrary.process("hello")
    println("Input: hello → Output: $result")

    println("\n--- Custom Config ---")
    val custom = MyLibrary.process("hello", Config(uppercase = true))
    println("Input: hello (uppercase) → Output: $custom")

    println("\n--- Edge Cases ---")
    println("Empty string: ${MyLibrary.process("")}")
    println("Unicode: ${MyLibrary.process("日本語")}")

    println("\n=== All demos passed ===")
}
```

### TestApp in settings.gradle.kts

```kotlin
// ALWAYS include testapp
include(":testapp")
// or for multi-platform samples:
include(":testapp:android")
include(":testapp:desktop")
```

### When Developing the Library

The workflow is:
1. Write/change library code
2. **Immediately run TestApp** to verify it works
3. If something is broken — fix it before moving on
4. Add new API → add new demo in TestApp in the same commit

---

# Process

## 🚀 High-Level Workflow

### Phase 1: Planning & API Design

The most important phase. A library's API is its contract with the world — changing it later is painful for everyone.

#### 1.1 Define the Library's Purpose

Before writing any code, answer these questions clearly:

- **What problem does this library solve?** — One sentence, no jargon.
- **Who is the target audience?** — Android devs? Backend devs? KMP projects?
- **What are the boundaries?** — What does the library NOT do?
- **What are the target platforms?** — JVM only? Android + iOS? All KMP targets?
- **What are the dependencies?** — Minimize them. Every dependency is a liability.

Write this down as a `DESIGN.md` document in the project root.

#### 1.2 Research Existing Solutions

Before building, study:
- Similar libraries in the Kotlin/Java ecosystem
- How the Kotlin standard library solves similar problems
- Convention patterns used in popular libraries (Ktor, kotlinx.serialization, Koin, Decompose, etc.)

Use web search to find:
- Current best practices for Kotlin library development
- Latest Gradle and Kotlin plugin versions
- Platform-specific requirements and limitations

#### 1.3 API-First Design

**Design the public API before writing implementation.**

Create an `api-sketch.kt` file with:
- Public classes, interfaces, and functions (signatures only)
- DSL builders if applicable
- Extension functions
- Expected usage examples as comments

Follow the **API Design Principles** in [📐 API Design Guide](./references/api-design.md).

Key principles:
- **Minimal surface area** — Expose only what's necessary
- **Discoverable** — Users should find what they need without docs
- **Consistent** — Same patterns everywhere
- **Hard to misuse** — Compiler catches mistakes, not runtime
- **Backward compatible** — Plan for evolution from day one

#### 1.4 Choose Architecture Pattern

Based on the library type:

| Library Type | Recommended Pattern |
|---|---|
| Utility / Extensions | Top-level functions + extension functions |
| State management | Interface + internal implementation |
| UI Components (Compose) | Composable functions + State holders |
| Networking / IO | Interface + Builder DSL + suspend functions |
| Gradle Plugin | Convention plugin + Extension DSL |
| Data processing | Pipeline / Chain pattern |

---

### Phase 2: Project Setup

#### 2.1 Project Structure

Read [📁 Project Structure Guide](./references/project-structure.md) for the complete template.

**For a KMP library:**
```
my-library/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle/
│   └── libs.versions.toml
├── gradle.properties
├── my-library-core/
│   ├── build.gradle.kts
│   └── src/
│       ├── commonMain/kotlin/
│       ├── commonTest/kotlin/
│       ├── androidMain/kotlin/
│       ├── iosMain/kotlin/
│       └── jvmMain/kotlin/
├── my-library-compose/              # Optional Compose module
│   ├── build.gradle.kts
│   └── src/
│       ├── commonMain/kotlin/
│       └── commonTest/kotlin/
├── testapp/                         # ⚠️ MANDATORY
│   ├── build.gradle.kts
│   └── src/main/
├── README.md
├── DESIGN.md
├── CHANGELOG.md
├── LICENSE
└── .github/
    └── workflows/
        └── ci.yml
```

**For a simple JVM library:**
```
my-library/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle/
│   └── libs.versions.toml
├── lib/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/kotlin/
│       └── test/kotlin/
├── testapp/                         # ⚠️ MANDATORY
│   ├── build.gradle.kts
│   └── src/main/kotlin/
│       └── Main.kt
├── README.md
└── CHANGELOG.md
```

#### 2.2 Gradle Configuration

**Critical settings in `gradle.properties`:**
```properties
kotlin.code.style=official
kotlin.mpp.stability.nowarn=true
kotlin.mpp.androidSourceSetLayoutVersion=2
org.gradle.jvmargs=-Xmx2048m
org.gradle.parallel=true
org.gradle.caching=true
```

**Use Version Catalog (`libs.versions.toml`):**
```toml
[versions]
kotlin = "2.1.0"
compose-multiplatform = "1.7.3"
coroutines = "1.10.1"

[libraries]
kotlinx-coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "coroutines" }

[plugins]
kotlin-multiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
```

**IMPORTANT:** Always search for the LATEST stable versions of Kotlin, Compose Multiplatform, AGP, and key dependencies before generating `libs.versions.toml`. Do not rely on memorized versions — they go stale fast.

#### 2.3 Module Design

Follow the **module separation principle**:

- `core` — Pure Kotlin logic, no platform dependencies
- `compose` — Compose UI components (depends on core)
- `android` — Android-specific extensions (depends on core)
- `ios` — iOS-specific extensions (depends on core)
- `testapp` — **Runnable demo app (MANDATORY, not published)**

Each published module = one Gradle module = one Maven artifact. TestApp is excluded from publishing.

---

### Phase 3: Implementation

#### 3.1 Kotlin Coding Standards

Read [📝 Coding Standards](./references/coding-standards.md) for the complete guide.

**Essential rules:**

**Visibility:**
- Default to `internal` or `private`. Only make `public` what's part of the API.
- Use `@PublishedApi internal` only when absolutely needed for inline functions.

**Nullability:**
- Prefer non-null types in the public API.
- Use `require()` and `check()` for preconditions, not `!!`.
- Return `null` only when absence is a valid, meaningful state.

**Coroutines:**
- Library functions should be `suspend` or accept `CoroutineScope`, never create their own scope silently.
- Use `withContext(Dispatchers.IO)` inside the library only when the operation is definitively IO-bound.
- Never hardcode dispatchers — accept them as parameters or use `Dispatchers.Default`.
- Provide `Flow`-based APIs for reactive data.

**Compose (if applicable):**
- Composable functions: `PascalCase`, noun-based names.
- State: expose via `State<T>`, remember internally.
- Follow Compose API guidelines: slot APIs, modifier parameter, default parameter values.

**Immutability:**
- Prefer `val` over `var`.
- Use immutable data classes for public models.
- Use `List`, `Set`, `Map` (not `MutableList` etc.) in public API.

**Naming:**
```kotlin
// Classes: PascalCase, nouns
class TooltipController

// Functions: camelCase, verbs
fun showTooltip()

// Constants: SCREAMING_SNAKE_CASE
const val DEFAULT_ANIMATION_DURATION = 300L

// DSL builders: lowercase, matching the noun
fun tooltip(block: TooltipBuilder.() -> Unit): Tooltip

// Extensions: should read like natural language
fun String.toSlug(): String
```

#### 3.2 DSL Design (if applicable)

Kotlin DSLs are a superpower for library APIs:

```kotlin
// 1. Use @DslMarker to prevent scope leaking
@DslMarker
annotation class MyLibraryDsl

// 2. Builder pattern with receiver
@MyLibraryDsl
class TooltipBuilder {
    var text: String = ""
    var position: Position = Position.Bottom

    fun style(block: StyleBuilder.() -> Unit) {
        styleBuilder.apply(block)
    }

    internal fun build(): TooltipConfig {
        require(text.isNotBlank()) { "Tooltip text must not be blank" }
        return TooltipConfig(text, position, styleBuilder.build())
    }
}

// 3. Top-level entry point
fun tooltip(block: TooltipBuilder.() -> Unit): TooltipConfig {
    return TooltipBuilder().apply(block).build()
}
```

#### 3.3 Error Handling

```kotlin
// 1. Define library-specific exceptions
public open class MyLibraryException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

// 2. Use Result for operations that can fail expectedly
public suspend fun fetchData(): Result<Data>

// 3. Document all thrown exceptions with @throws
```

#### 3.4 KMP expect/actual Pattern

```kotlin
// commonMain
public expect class PlatformLogger() {
    public fun log(message: String)
}

// androidMain
public actual class PlatformLogger {
    public actual fun log(message: String) {
        Log.d("MyLibrary", message)
    }
}
```

**Minimize expect/actual surface.** Use interfaces + factory functions instead when possible:

```kotlin
// Prefer this:
public interface Logger {
    fun log(message: String)
}

internal expect fun createPlatformLogger(): Logger
```

---

### Phase 4: TestApp Development

**This phase is as important as the library itself.**

The TestApp is the living proof that the library works. It is also the best documentation a developer can get — runnable examples.

#### 4.1 Build the TestApp Alongside the Library

Do NOT write the whole library first and then make a testapp. Instead:

1. Create library module + testapp module at the same time
2. Implement a feature in the library
3. Immediately add a demo for it in the testapp
4. Run the testapp, verify visually / in console
5. Repeat

This ensures you catch issues immediately and design user-friendly APIs (because you ARE the first user).

#### 4.2 TestApp for Compose Libraries

```kotlin
// testapp/src/main/kotlin/.../MainActivity.kt
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                TestAppNavigation()
            }
        }
    }
}

@Composable
fun TestAppNavigation() {
    var currentScreen by remember { mutableStateOf("main") }

    when (currentScreen) {
        "main" -> MainScreen(onNavigate = { currentScreen = it })
        "basic" -> BasicUsageScreen(onBack = { currentScreen = "main" })
        "advanced" -> AdvancedUsageScreen(onBack = { currentScreen = "main" })
        "edge_cases" -> EdgeCasesScreen(onBack = { currentScreen = "main" })
    }
}

@Composable
fun MainScreen(onNavigate: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                "MyLibrary TestApp",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                "Tap any section to see demos",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
        }

        item {
            DemoCard(
                title = "Basic Usage",
                description = "Core functionality with default settings",
                onClick = { onNavigate("basic") }
            )
        }

        item {
            DemoCard(
                title = "Advanced Usage",
                description = "Custom configuration, theming, animations",
                onClick = { onNavigate("advanced") }
            )
        }

        item {
            DemoCard(
                title = "Edge Cases",
                description = "Long text, RTL, rapid interactions, empty states",
                onClick = { onNavigate("edge_cases") }
            )
        }
    }
}
```

#### 4.3 TestApp for Non-Compose Libraries

```kotlin
// testapp/src/main/kotlin/Main.kt
fun main() {
    println("╔══════════════════════════════════╗")
    println("║     MyLibrary TestApp v1.0.0     ║")
    println("╚══════════════════════════════════╝\n")

    runDemo("Basic Usage") {
        val client = MyLibraryClient()
        val result = client.process("hello world")
        println("  Input:  \"hello world\"")
        println("  Output: \"$result\"")
        check(result == "HELLO WORLD") { "Expected uppercase!" }
    }

    runDemo("Custom Config") {
        val client = MyLibraryClient(
            config = Config(separator = "-", trim = true)
        )
        val result = client.process("  hello world  ")
        println("  Input:  \"  hello world  \" (trim=true, sep=\"-\")")
        println("  Output: \"$result\"")
    }

    runDemo("Error Handling") {
        try {
            MyLibraryClient().process("")
            println("  ❌ Should have thrown!")
        } catch (e: IllegalArgumentException) {
            println("  ✅ Correctly threw: ${e.message}")
        }
    }

    runDemo("Flow / Reactive API") {
        runBlocking {
            val client = MyLibraryClient()
            client.observeState().take(3).collect { state ->
                println("  State: $state")
            }
        }
    }

    println("\n✅ All demos completed successfully!")
}

private fun runDemo(name: String, block: () -> Unit) {
    println("── $name ${"─".repeat(maxOf(0, 40 - name.length))}")
    try {
        block()
    } catch (e: Exception) {
        println("  ❌ FAILED: ${e.message}")
    }
    println()
}
```

#### 4.4 What the TestApp Must Cover

| Category | Examples |
|---|---|
| **Happy path** | Default config, typical input, standard usage |
| **All public API** | Every public class, function, composable, DSL |
| **Customization** | Custom styles, themes, configs, parameters |
| **Edge cases** | Empty input, very long input, special characters, RTL |
| **Error states** | Invalid input, network errors, missing resources |
| **Lifecycle** | Rotation, background/foreground, recomposition (Compose) |
| **Interop** | Usage alongside other popular libraries if relevant |

---

### Phase 5: Documentation

#### 5.1 KDoc

Every public declaration MUST have KDoc:

```kotlin
/**
 * Controls the lifecycle and visibility of tutorial tooltips.
 *
 * Create an instance using [rememberTooltipController] in a Composable context,
 * or manually via the constructor for non-Compose usage.
 *
 * ## Usage
 *
 * ```kotlin
 * val controller = rememberTooltipController()
 * controller.show(
 *     target = myButtonBounds,
 *     text = "Tap here to continue"
 * )
 * ```
 *
 * @property isVisible Whether a tooltip is currently displayed.
 * @see TooltipConfig
 * @see rememberTooltipController
 * @since 1.0.0
 */
public class TooltipController { ... }
```

#### 5.2 README.md Structure

```markdown
# Library Name

One-line description of what it does.

[![Maven Central](badge-url)](link)
[![Build](badge-url)](link)

## Features
- Feature 1
- Feature 2

## Installation

### Kotlin Multiplatform
​```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("group:artifact:version")
        }
    }
}
​```

### Android / JVM
​```kotlin
dependencies {
    implementation("group:artifact:version")
}
​```

## Quick Start
// Minimal working example

## Running the TestApp
​```bash
# Android
./gradlew :testapp:installDebug

# Desktop
./gradlew :testapp:run

# JVM
./gradlew :testapp:run
​```

## Documentation
Link to full docs / wiki / API reference

## Compatibility
| Platform | Min Version |
|---|---|
| Android | API 21 |
| iOS | 15.0 |
| JVM | 11 |

## License
```

#### 5.3 CHANGELOG.md

Follow [Keep a Changelog](https://keepachangelog.com/) format:

```markdown
# Changelog

## [Unreleased]

## [1.1.0] - 2025-03-15
### Added
- New `TooltipStyle.Custom` for fully custom styling

### Changed
- Improved animation performance on iOS

### Fixed
- Tooltip position incorrect when keyboard is visible (#42)
```

---

### Phase 6: Publishing

#### 6.1 Maven Central / GitHub Packages

Read [📦 Publishing Guide](./references/publishing.md) for the complete guide.

**Use [vanniktech/gradle-maven-publish-plugin](https://github.com/vanniktech/gradle-maven-publish-plugin)** — it simplifies KMP publishing enormously.

**IMPORTANT:** Exclude TestApp from publishing:

```kotlin
// Root build.gradle.kts
subprojects {
    if (name == "testapp") return@subprojects

    // Apply publishing only to library modules
    if (name.startsWith("my-library-")) {
        apply(plugin = "com.vanniktech.maven.publish")
        // ...
    }
}
```

#### 6.2 Versioning

Follow [Semantic Versioning](https://semver.org/):
- **MAJOR** (1.0.0 → 2.0.0): Breaking API changes
- **MINOR** (1.0.0 → 1.1.0): New features, backward compatible
- **PATCH** (1.0.0 → 1.0.1): Bug fixes only

Pre-release: `1.0.0-alpha01`, `1.0.0-beta01`, `1.0.0-rc01`

#### 6.3 CI/CD

Minimum CI pipeline:
```yaml
name: CI
on: [push, pull_request]
jobs:
  build:
    runs-on: macos-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - uses: gradle/actions/setup-gradle@v4
      - run: ./gradlew build
      - run: ./gradlew :testapp:assembleDebug  # ⚠️ Always verify testapp builds
```

---

# Reference Files

## 📚 Documentation Library

Load these resources as needed during development:

### Core Guides (Load During Phase 1-2)
- [📐 API Design Guide](./references/api-design.md) — Public API design principles, Kotlin idioms, DSL patterns, Compose API conventions
- [📁 Project Structure Guide](./references/project-structure.md) — Module layout, Gradle config templates, version catalogs

### Implementation Guides (Load During Phase 3)
- [📝 Coding Standards](./references/coding-standards.md) — Code style, patterns, anti-patterns, Compose guidelines

### Publishing Guides (Load During Phase 6)
- [📦 Publishing Guide](./references/publishing.md) — Maven Central, signing, CI/CD, versioning
