# Publishing Guide for Kotlin Libraries

## Publishing Options

| Target | Best For | Complexity |
|---|---|---|
| **Maven Central** | Open source libraries | Medium |
| **GitHub Packages** | Private / organization libraries | Low |
| **JitPack** | Quick prototypes, personal projects | Very Low |
| **Custom Maven Repo** | Enterprise / corporate | High |

---

## Maven Central Publishing

### Prerequisites

1. **Sonatype OSSRH account** → Register at https://central.sonatype.com
2. **GPG key** → For signing artifacts
3. **Namespace verification** → Prove ownership of `io.github.username` or your domain

### Using vanniktech/gradle-maven-publish-plugin (Recommended)

This plugin handles 90% of the boilerplate:

```toml
# libs.versions.toml
[plugins]
maven-publish = { id = "com.vanniktech.maven.publish", version = "0.30.0" }
```

```kotlin
// Published module's build.gradle.kts
plugins {
    alias(libs.plugins.maven.publish)
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    coordinates(
        groupId = "io.github.username",
        artifactId = "my-library-core",
        version = "1.0.0"
    )

    pom {
        name.set("My Library Core")
        description.set("Core module of My Library — a Kotlin library for X")
        inceptionYear.set("2025")
        url.set("https://github.com/username/my-library")

        licenses {
            license {
                name.set("Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("repo")
            }
        }

        developers {
            developer {
                id.set("username")
                name.set("Your Name")
                url.set("https://github.com/username")
            }
        }

        scm {
            url.set("https://github.com/username/my-library")
            connection.set("scm:git:git://github.com/username/my-library.git")
            developerConnection.set("scm:git:ssh://git@github.com/username/my-library.git")
        }
    }
}
```

### Secrets Configuration

Store secrets in `~/.gradle/gradle.properties` (NEVER in the repo):

```properties
mavenCentralUsername=your-sonatype-username
mavenCentralPassword=your-sonatype-password
signing.keyId=LAST8CHARS
signing.password=your-gpg-passphrase
signing.secretKeyRingFile=/path/to/.gnupg/secring.gpg
```

Or use environment variables in CI:

```properties
# CI reads from env vars
ORG_GRADLE_PROJECT_mavenCentralUsername
ORG_GRADLE_PROJECT_mavenCentralPassword
ORG_GRADLE_PROJECT_signingInMemoryKey
ORG_GRADLE_PROJECT_signingInMemoryKeyId
ORG_GRADLE_PROJECT_signingInMemoryKeyPassword
```

### Publishing Commands

```bash
# Publish to Maven Central staging
./gradlew publishAllPublicationsToMavenCentralRepository

# Publish and automatically release (skip manual staging)
./gradlew publishAndReleaseToMavenCentral
```

---

## Versioning Strategy

### Semantic Versioning

```
MAJOR.MINOR.PATCH[-PRE_RELEASE]

1.0.0         → First stable release
1.1.0         → New features, backward compatible
1.1.1         → Bug fix
2.0.0         → Breaking changes
1.2.0-alpha01 → Pre-release alpha
1.2.0-beta01  → Pre-release beta
1.2.0-rc01    → Release candidate
```

### Version Management

**Option A: Single source of truth in `gradle.properties`**

```properties
# gradle.properties
VERSION_NAME=1.2.0
VERSION_CODE=12 # for Android
```

```kotlin
// build.gradle.kts
version = findProperty("VERSION_NAME") as String
```

**Option B: Version catalog**

```toml
[versions]
my-library = "1.2.0"
```

### Pre-release Strategy

```
Development:    1.0.0-SNAPSHOT (never publish to Central)
Alpha:          1.0.0-alpha01, -alpha02, ...  (API may change)
Beta:           1.0.0-beta01, -beta02, ...    (API stabilizing)
Release Candidate: 1.0.0-rc01, -rc02, ...     (API frozen, bug fixes only)
Stable:         1.0.0                          (Production ready)
```

---

## CI/CD with GitHub Actions

### CI Pipeline (Every Push/PR)

```yaml
# .github/workflows/ci.yml
name: CI

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: macos-latest  # Required for iOS targets
    
    steps:
      - uses: actions/checkout@v4
      
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - uses: gradle/actions/setup-gradle@v4
      
      - name: Build
        run: ./gradlew build
      
      - name: Check API compatibility
        run: ./gradlew apiCheck
      
      - name: Run tests
        run: ./gradlew allTests
      
      - name: Check code coverage
        run: ./gradlew koverVerify
      
      - name: Upload test reports
        if: failure()
        uses: actions/upload-artifact@v4
        with:
          name: test-reports
          path: '**/build/reports/tests/'
```

### Release Pipeline (On Tag)

```yaml
# .github/workflows/publish.yml
name: Publish

on:
  push:
    tags:
      - 'v*'

jobs:
  publish:
    runs-on: macos-latest
    
    steps:
      - uses: actions/checkout@v4
      
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - uses: gradle/actions/setup-gradle@v4
      
      - name: Build & Test
        run: ./gradlew build allTests apiCheck
      
      - name: Publish to Maven Central
        run: ./gradlew publishAndReleaseToMavenCentral
        env:
          ORG_GRADLE_PROJECT_mavenCentralUsername: ${{ secrets.MAVEN_CENTRAL_USERNAME }}
          ORG_GRADLE_PROJECT_mavenCentralPassword: ${{ secrets.MAVEN_CENTRAL_PASSWORD }}
          ORG_GRADLE_PROJECT_signingInMemoryKey: ${{ secrets.SIGNING_KEY }}
          ORG_GRADLE_PROJECT_signingInMemoryKeyId: ${{ secrets.SIGNING_KEY_ID }}
          ORG_GRADLE_PROJECT_signingInMemoryKeyPassword: ${{ secrets.SIGNING_KEY_PASSWORD }}
      
      - name: Create GitHub Release
        uses: softprops/action-gh-release@v2
        with:
          generate_release_notes: true
```

### Release Process

1. Update version in `gradle.properties`
2. Update `CHANGELOG.md`
3. Commit: `git commit -m "Release v1.2.0"`
4. Tag: `git tag v1.2.0`
5. Push: `git push origin main --tags`
6. CI publishes automatically

---

## Multi-Module Publishing

For a library with multiple modules:

```kotlin
// Root build.gradle.kts — share common POM config
subprojects {
    // Only apply to published modules
    if (name.startsWith("my-library-")) {
        apply(plugin = "com.vanniktech.maven.publish")
        
        mavenPublishing {
            publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
            signAllPublications()
        }
    }
}
```

Each module gets its own artifact:
- `io.github.username:my-library-core:1.0.0`
- `io.github.username:my-library-compose:1.0.0`

**Always publish all modules with the same version** to avoid compatibility issues.

---

## BOM (Bill of Materials) — Optional

For libraries with many modules, create a BOM:

```kotlin
// my-library-bom/build.gradle.kts
plugins {
    `java-platform`
    alias(libs.plugins.maven.publish)
}

dependencies {
    constraints {
        api(project(":my-library-core"))
        api(project(":my-library-compose"))
    }
}
```

Users can then:
```kotlin
dependencies {
    implementation(platform("io.github.username:my-library-bom:1.0.0"))
    implementation("io.github.username:my-library-core") // no version needed
    implementation("io.github.username:my-library-compose")
}
```
