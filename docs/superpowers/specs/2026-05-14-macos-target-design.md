# Добавление таргета macOS в DailyLog

**Дата:** 2026-05-14
**Статус:** approved (готов к writing-plans)

## Цель

Добавить поддержку macOS в Kotlin Multiplatform библиотеку DailyLog,
чтобы её можно было использовать из macOS-приложений и публиковать соответствующие
артефакты в Maven Central.

## Решения

| Развилка | Решение |
|----------|---------|
| Архитектуры | `macosArm64` + `macosX64` |
| Структура source sets | Промежуточный `appleMain`, общий для iOS и macOS |
| API вывода | `NSLog` — единая реализация в `appleMain` (как iOS) |
| Sample-приложение | Не включаем в этот тикет |
| Тесты | Не добавляем в этот тикет |

## Объём работ

### 1. `library/build.gradle.kts`

Добавить таргеты после блока iOS:

```kotlin
iosX64()
iosArm64()
iosSimulatorArm64()
macosX64()
macosArm64()
```

Дефолтная иерархия source sets KMP (`applyDefaultHierarchyTemplate`,
включена по умолчанию) автоматически создаёт `appleMain` как родителя для
`iosMain` и `macosMain` при наличии обоих наборов таргетов.

### 2. Source sets

Итоговая структура:

```
commonMain
├── appleMain              (новый — общая Apple-реализация)
│   ├── iosMain
│   │   ├── iosX64Main
│   │   ├── iosArm64Main
│   │   └── iosSimulatorArm64Main
│   └── macosMain
│       ├── macosX64Main
│       └── macosArm64Main
├── androidMain
└── jvmMain
```

`macosMain` остаётся пустым — `appleMain` полностью закрывает `expect fun platformLogOutput`.

### 3. Перенос реализации

- Создать `library/src/appleMain/kotlin/pro/dailycode/dailylog/LogOutput.apple.kt`
  с текущим содержимым `LogOutput.ios.kt` (NSLog + heart-эмодзи).
- Удалить `library/src/iosMain/kotlin/pro/dailycode/dailylog/LogOutput.ios.kt`.

Поведение на macOS идентично iOS:

```
🤍 [tag] message    // DEBUG
💙 [tag] message    // INFO
💛 [tag] message    // WARNING
❤️ [tag] message    // ERROR
```

Вывод через `NSLog` виден в Console.app и stderr (Xcode/Terminal).

### 4. README.md

- В **Features** заменить «Android, iOS, JVM» → «Android, iOS, macOS, JVM».
- В **Platform-native output** добавить строку про macOS:
  `**macOS** — NSLog (тот же формат, что iOS)`.
- В **Compatibility** добавить строку:
  | macOS | 11.0 |

### 5. Maven-публикация

Дополнительной настройки не требуется. `vanniktech.mavenPublish`
автоматически создаёт артефакты `dailylog-macosx64` и `dailylog-macosarm64`
при добавлении таргетов.

## Вне скоупа

- Sample-приложение для macOS (`:sample:macos`).
- Тесты (`commonTest` / `appleTest` / `macosTest`).
- Переход на `os_log` / `OSLog` вместо `NSLog`.
- Bump версии и публикация релиза.

## Верификация

Команды для проверки сборки:

- `./gradlew :library:compileKotlinMacosArm64`
- `./gradlew :library:compileKotlinMacosX64`
- `./gradlew :library:assemble`

Регрессионные проверки (существующие таргеты не должны сломаться):

- `./gradlew :library:compileKotlinIosArm64`
- `./gradlew :library:compileKotlinJvm`
- `./gradlew :library:assembleDebug` (Android)

## Риски

- **Иерархия source sets**: если в проекте по какой-то причине отключён
  `applyDefaultHierarchyTemplate`, потребуется явно прописать
  `appleMain.dependsOn(commonMain)` и подключение к iOS/macOS-таргетам.
  Проверить на этапе имплементации.
- **Min macOS**: 11.0 — дефолт Kotlin/Native для современных версий.
  Если потребуется ниже, указывается через `binaries` блок (в текущем
  тикете не требуется).
