# macOS Target Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Добавить таргет macOS (arm64 + x64) в KMP-библиотеку DailyLog, переиспользовав iOS-реализацию `NSLog` через общий `appleMain` source set.

**Architecture:** Вводим промежуточный `appleMain` source set между `commonMain` и Apple-таргетами (`iosMain`, `macosMain`). Переносим текущую iOS-реализацию `platformLogOutput` (NSLog + heart-эмодзи) в `appleMain` — она автоматически становится actual'ом и для iOS, и для macOS. macOS не получает собственного `LogOutput`-файла.

**Tech Stack:** Kotlin Multiplatform, Kotlin/Native, Gradle (Kotlin DSL), vanniktech.mavenPublish, Platform.Foundation.NSLog.

**Спецификация:** `docs/superpowers/specs/2026-05-14-macos-target-design.md`

**Важно про коммиты:** Пользователь явно просил не делать `git commit` без отдельной команды. Шаги Commit оставлены в плане как стандартный TDD-ритм, но при исполнении агентом — пропускать их, пока пользователь не подтвердит.

---

## File Structure

**Create:**
- `library/src/appleMain/kotlin/pro/dailycode/dailylog/LogOutput.apple.kt` — общая Apple-реализация `platformLogOutput` (NSLog + heart-эмодзи).

**Modify:**
- `library/build.gradle.kts` — добавление `macosX64()` и `macosArm64()` после `iosSimulatorArm64()`.
- `README.md` — Features, Platform Output Examples (опционально), Compatibility.

**Delete:**
- `library/src/iosMain/kotlin/pro/dailycode/dailylog/LogOutput.ios.kt` — содержимое уезжает в `appleMain`.

---

## Task 1: Добавить таргеты macOS в build.gradle.kts

**Files:**
- Modify: `library/build.gradle.kts:31-33`

- [ ] **Step 1: Добавить вызовы `macosX64()` и `macosArm64()`**

Открыть `library/build.gradle.kts`. Найти секцию:

```kotlin
    iosX64()
    iosArm64()
    iosSimulatorArm64()
```

Заменить на:

```kotlin
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    macosX64()
    macosArm64()
```

- [ ] **Step 2: Проверить, что Gradle видит новые таргеты**

Run: `./gradlew :library:tasks --all | grep -i macos`

Expected: в выводе появляются таски типа `compileKotlinMacosArm64`, `compileKotlinMacosX64`, `linkDebugFrameworkMacosArm64`, `macosArm64MainKlibrary` и т.п.

- [ ] **Step 3: Запустить компиляцию текущего состояния (ожидаем падение)**

Run: `./gradlew :library:compileKotlinMacosArm64`

Expected: **FAIL** с ошибкой вроде `Expected function 'platformLogOutput' has no actual declaration in module ... for Native (macos_arm64)`. Это нормально — `expect` ещё не закрыт для macOS. Закрытие — в Task 2.

- [ ] **Step 4: Commit (пропустить, если пользователь не разрешил)**

```bash
git add library/build.gradle.kts
git commit -m "build: add macosX64 and macosArm64 targets"
```

---

## Task 2: Перенести реализацию NSLog в appleMain

**Files:**
- Create: `library/src/appleMain/kotlin/pro/dailycode/dailylog/LogOutput.apple.kt`
- Delete: `library/src/iosMain/kotlin/pro/dailycode/dailylog/LogOutput.ios.kt`

- [ ] **Step 1: Создать `LogOutput.apple.kt` в appleMain**

Создать файл `library/src/appleMain/kotlin/pro/dailycode/dailylog/LogOutput.apple.kt` со следующим содержимым:

```kotlin
package pro.dailycode.dailylog

import platform.Foundation.NSLog

/**
 * Apple (iOS & macOS) log output with heart emojis representing log level severity.
 *
 * - DEBUG:   🤍 (white heart)
 * - INFO:    💙 (blue heart)
 * - WARNING: 💛 (yellow heart)
 * - ERROR:   ❤️ (red heart)
 */
public actual fun platformLogOutput(level: LogLevel, tag: String, message: String) {
    val heart = when (level) {
        LogLevel.DEBUG -> "🤍"
        LogLevel.INFO -> "💙"
        LogLevel.WARNING -> "💛"
        LogLevel.ERROR -> "❤️"
    }
    NSLog("$heart [$tag] $message")
}
```

- [ ] **Step 2: Удалить старый `LogOutput.ios.kt`**

Run: `rm library/src/iosMain/kotlin/pro/dailycode/dailylog/LogOutput.ios.kt`

Если в `iosMain/kotlin/pro/dailycode/dailylog/` после этого станет пусто, оставить пустую папку — Gradle с этим спокоен; принудительно удалять иерархию не нужно.

- [ ] **Step 3: Запустить чистую сборку macOS-таргетов**

Run: `./gradlew :library:compileKotlinMacosArm64 :library:compileKotlinMacosX64`

Expected: **PASS**, компиляция успешна для обеих архитектур.

Если упало с сообщением про отсутствие `appleMain` source set / `applyDefaultHierarchyTemplate` — значит в проекте кастомная иерархия. Тогда добавить в `kotlin { ... }` блок:

```kotlin
applyDefaultHierarchyTemplate()
```

и повторить шаг. (В современных версиях KMP-плагина это default, см. spec.)

- [ ] **Step 4: Регрессия: убедиться, что iOS-таргеты по-прежнему компилируются**

Run: `./gradlew :library:compileKotlinIosArm64 :library:compileKotlinIosX64 :library:compileKotlinIosSimulatorArm64`

Expected: **PASS** для всех трёх. Реализация `platformLogOutput` теперь приходит из `appleMain` и должна закрывать `expect` идентично прежнему `iosMain`.

- [ ] **Step 5: Регрессия: убедиться, что Android и JVM не сломались**

Run: `./gradlew :library:compileKotlinJvm :library:assembleDebug`

Expected: **PASS** для обоих. Android и JVM не трогали, но команда подтверждает отсутствие случайной поломки конфигурации.

- [ ] **Step 6: Полная сборка библиотеки**

Run: `./gradlew :library:assemble`

Expected: **PASS**. Артефакты для всех таргетов собираются.

- [ ] **Step 7: Commit (пропустить, если пользователь не разрешил)**

```bash
git add library/src/appleMain/kotlin/pro/dailycode/dailylog/LogOutput.apple.kt
git add library/src/iosMain/kotlin/pro/dailycode/dailylog/LogOutput.ios.kt
git commit -m "refactor: share NSLog implementation via appleMain"
```

(`git add` на удалённый файл регистрирует удаление; если в git-клиенте проще — `git rm library/src/iosMain/kotlin/pro/dailycode/dailylog/LogOutput.ios.kt`.)

---

## Task 3: Обновить README.md

**Files:**
- Modify: `README.md:7` (Features — Multiplatform line)
- Modify: `README.md:13-17` (Platform-native output)
- Modify: `README.md:167-171` (Compatibility table)

- [ ] **Step 1: Обновить Features**

Найти строку:

```markdown
- **Multiplatform** — Android, iOS, JVM
```

Заменить на:

```markdown
- **Multiplatform** — Android, iOS, macOS, JVM
```

- [ ] **Step 2: Добавить macOS в Platform-native output**

Найти блок:

```markdown
- **Platform-native output**:
  - **Android** — `android.util.Log`
  - **iOS** — `NSLog` with heart emojis: 🤍 DEBUG, 💙 INFO, 💛 WARNING, ❤️ ERROR
  - **JVM** — `stdout` / `stderr`
```

Заменить на:

```markdown
- **Platform-native output**:
  - **Android** — `android.util.Log`
  - **iOS / macOS** — `NSLog` with heart emojis: 🤍 DEBUG, 💙 INFO, 💛 WARNING, ❤️ ERROR
  - **JVM** — `stdout` / `stderr`
```

- [ ] **Step 3: Добавить macOS в Compatibility**

Найти таблицу:

```markdown
| Platform | Min Version |
|----------|-------------|
| Android  | API 24      |
| iOS      | 15.0        |
| JVM      | 11          |
```

Заменить на:

```markdown
| Platform | Min Version |
|----------|-------------|
| Android  | API 24      |
| iOS      | 15.0        |
| macOS    | 11.0        |
| JVM      | 11          |
```

- [ ] **Step 4: Визуально проверить рендер**

Открыть `README.md` в редакторе/просмотрщике. Убедиться, что:
- В Features упомянут macOS.
- Строка про NSLog покрывает iOS и macOS вместе.
- В таблице Compatibility 4 строки, macOS — между iOS и JVM.

- [ ] **Step 5: Commit (пропустить, если пользователь не разрешил)**

```bash
git add README.md
git commit -m "docs: document macOS support in README"
```

---

## Task 4: Финальная верификация

**Files:** (нет изменений — только проверки)

- [ ] **Step 1: Чистая сборка с нуля**

Run: `./gradlew clean :library:assemble`

Expected: **PASS**. Все таргеты (Android, JVM, iosX64/Arm64/SimulatorArm64, macosX64/Arm64) собираются с чистого состояния.

- [ ] **Step 2: Проверить публикуемые артефакты**

Run: `./gradlew :library:publishToMavenLocal`

Expected: **PASS**. В `~/.m2/repository/pro/dailycode/` должны появиться (помимо прежних) папки:
- `dailylog-macosx64/0.9.5/`
- `dailylog-macosarm64/0.9.5/`

Run: `ls ~/.m2/repository/pro/dailycode/`

Expected: список включает `dailylog-macosx64` и `dailylog-macosarm64`.

- [ ] **Step 3: Sanity-check Gradle-конфига**

Run: `./gradlew :library:dependencies --configuration macosArm64MainImplementation`

Expected: **PASS** без ошибок, отображается дерево зависимостей (даже если оно пустое — главное, что конфигурация существует и резолвится).

- [ ] **Step 4: Не осталось ли мусора**

Run: `find library/src/iosMain -name "LogOutput*"`

Expected: пусто (старый файл удалён).

Run: `find library/src/appleMain -name "LogOutput*"`

Expected: `library/src/appleMain/kotlin/pro/dailycode/dailylog/LogOutput.apple.kt`.

- [ ] **Step 5: Итоговый commit для документации (пропустить, если пользователь не разрешил)**

Если на предыдущих тасках коммиты пропускались — собрать всё одним коммитом:

```bash
git add library/build.gradle.kts \
        library/src/appleMain/kotlin/pro/dailycode/dailylog/LogOutput.apple.kt \
        library/src/iosMain/kotlin/pro/dailycode/dailylog/LogOutput.ios.kt \
        README.md
git commit -m "feat: add macOS target (macosX64 + macosArm64)"
```

---

## Definition of Done

- `./gradlew :library:assemble` зелёный.
- `compileKotlinMacosArm64` и `compileKotlinMacosX64` зелёные.
- `compileKotlinIosArm64`, `compileKotlinIosX64`, `compileKotlinIosSimulatorArm64` зелёные (регрессия).
- `compileKotlinJvm` и Android `assembleDebug` зелёные (регрессия).
- `publishToMavenLocal` создаёт артефакты `dailylog-macosx64` и `dailylog-macosarm64`.
- README отражает поддержку macOS в Features, Platform-native output и Compatibility.
- Файла `library/src/iosMain/.../LogOutput.ios.kt` больше нет, его роль выполняет `library/src/appleMain/.../LogOutput.apple.kt`.
