# DailyLog

A lightweight Kotlin Multiplatform logging library with tag support and platform-specific formatting.

## Features

- **Multiplatform** — Android, iOS, JVM
- **Singleton** — single `DailyLogger` object, ready to use anywhere
- **Short API** — `d()`, `i()`, `w()`, `e()`
- **Tag support** — default tag + per-message override
- **Prefix** — optional global prefix: `[prefix/tag]`
- **Message prefix** — optional per-message prefix: `messagePrefix: message`
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

DailyLogger.d("Preparing request")
DailyLogger.i("GET /api/users -> 200 OK")
DailyLogger.w("Slow response: 3200ms")
DailyLogger.e("Connection timeout")
```

## Configuration

```kotlin
// Set default tag (used when no tag is passed)
DailyLogger.tag = "MyApp"

// Set optional global prefix — output: [MyApp/tag]
DailyLogger.prefix = "MyApp"

// Set minimum log level
DailyLogger.minLevel = LogLevel.WARNING
```

## Tag Override

Pass a `tag` to override the default per message:

```kotlin
DailyLogger.tag = "App"

// Uses default tag "App"
DailyLogger.i("Starting up")

// Override tag per message
DailyLogger.i("Connected", tag = "Database")
DailyLogger.e("Cache miss", tag = "Cache")
```

## Prefix

Set a global `prefix` to group logs: output becomes `[prefix/tag]`.

```kotlin
DailyLogger.prefix = "MyApp"
DailyLogger.tag = "Network"

DailyLogger.i("Request sent")
// JVM output: INFO [MyApp/Network] Request sent

DailyLogger.e("Timeout", tag = "Database")
// JVM output: ERROR [MyApp/Database] Timeout
```

When `prefix` is `null` or empty, the format is `[tag]` as usual.

## Message Prefix

Add a per-message prefix with `messagePrefix` — prepended to the message as `messagePrefix: message`:

```kotlin
DailyLogger.d("Connecting to db", messagePrefix = "Init")
// JVM output: DEBUG [App] Init: Connecting to db

DailyLogger.e("Table not found", tag = "Database", messagePrefix = "SQL")
// JVM output: ERROR [Database] SQL: Table not found
```

## Log Level Filtering

Set a minimum log level to suppress less severe messages:

```kotlin
DailyLogger.minLevel = LogLevel.WARNING

DailyLogger.d("ignored")  // not printed
DailyLogger.i("ignored")  // not printed
DailyLogger.w("printed")  // printed
DailyLogger.e("printed")  // printed
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

## API Reference

| Method | Level |
|--------|-------|
| `d(message, tag?, messagePrefix?)` | DEBUG |
| `i(message, tag?, messagePrefix?)` | INFO |
| `w(message, tag?, messagePrefix?)` | WARNING |
| `e(message, tag?, messagePrefix?)` | ERROR |
| `log(level, message, tag?, messagePrefix?)` | any |

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `tag` | `String` | `"DailyLogger"` | Default tag |
| `prefix` | `String?` | `null` | Global prefix: `[prefix/tag]` |
| `minLevel` | `LogLevel` | `DEBUG` | Minimum log level |

## Compatibility

| Platform | Min Version |
|----------|-------------|
| Android  | API 24      |
| iOS      | 15.0        |
| JVM      | 11          |

## License

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
