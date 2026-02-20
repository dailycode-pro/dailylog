# DailyLog

A lightweight Kotlin Multiplatform logging library with tag support and platform-specific formatting.

## Features

- **Multiplatform** — Android, iOS, JVM
- **Singleton** — single `DailyLogger` object, ready to use anywhere
- **Tag support** — default tag + per-message override
- **Log level filtering** — `DEBUG`, `INFO`, `WARNING`, `ERROR`
- **Platform-native output**:
  - **Android** — `android.util.Log`
  - **iOS** — `NSLog` with heart emojis: 🤍 DEBUG, 💙 INFO, 💛 WARNING, ❤️ ERROR
  - **JVM** — `stdout` / `stderr`
- **Zero dependencies** — pure Kotlin, no third-party libs

## Installation

### Kotlin Multiplatform

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("pro.dailycode:dailylog:<version>")
        }
    }
}
```

### Android / JVM

```kotlin
dependencies {
    implementation("pro.dailycode:dailylog:<version>")
}
```

## Quick Start

```kotlin
import pro.dailycode.dailylog.DailyLogger
import pro.dailycode.dailylog.LogLevel

// Use directly — DailyLogger is a singleton
DailyLogger.debug("Network", "Preparing request")
DailyLogger.info("Network", "GET /api/users -> 200 OK")
DailyLogger.warning("Network", "Slow response: 3200ms")
DailyLogger.error("Network", "Connection timeout")
```

## Configuration

```kotlin
// Set default tag (used when no tag is passed)
DailyLogger.tag = "MyApp"

// Set minimum log level
DailyLogger.minLevel = LogLevel.WARNING
```

## Tag Override

Pass a tag as the first argument, message as the last:

```kotlin
DailyLogger.tag = "App"

// Uses default tag "App"
DailyLogger.info(message = "Starting up")

// Override tag per message
DailyLogger.info("Database", "Connected")
DailyLogger.error("Cache", "Cache miss")
```

## Log Level Filtering

Set a minimum log level to suppress less severe messages:

```kotlin
DailyLogger.tag = "Prod"
DailyLogger.minLevel = LogLevel.WARNING

DailyLogger.debug(message = "ignored")   // not printed
DailyLogger.info(message = "ignored")    // not printed
DailyLogger.warning(message = "printed") // WARN [Prod] printed
DailyLogger.error(message = "printed")   // ERROR [Prod] printed
```

## Platform Output Examples

### Android
```
D/Network: GET /api/users -> 200 OK
W/Network: Slow response: 3200ms
E/Network: Connection timeout
```

### iOS
```
🤍 [Network] Preparing request
💙 [Network] GET /api/users -> 200 OK
💛 [Network] Slow response: 3200ms
❤️ [Network] Connection timeout
```

### JVM
```
DEBUG [Network] Preparing request
INFO [Network] GET /api/users -> 200 OK
WARN [Network] Slow response: 3200ms
ERROR [Network] Connection timeout
```

## Compatibility

| Platform | Min Version |
|----------|-------------|
| Android  | API 24      |
| iOS      | 15.0        |
| JVM      | 11          |

## 📄 Лицензия

```
Copyright 2025 DailyCode

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

---

<p align="center">
  Made with ❤️ by <a href="https://dailycode.pro">DailyCode</a>
</p>
