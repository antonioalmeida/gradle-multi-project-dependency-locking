# dependency-lock-all (Gradle plugin)

Gradle has built-in [dependency locking](https://docs.gradle.org/current/userguide/dependency_locking.html), but using it in a Multi Project builds is awkward. You have to enable `dependencyLocking { lockAllConfigurations() }` in every subproject, then iterate over each one with `--write-locks`. Most people end up writing a shell script that loops through subprojects calling `dependencies --write-locks` on each.

This plugin replaces that script. Apply it to your root project and run:

```bash
./gradlew writeDependencyLocks --write-locks
```

That's it — `gradle.lockfile` gets generated in each project directory.

## Installation

> [!WARNING]
> This plugin is under development and not yet published to the Gradle Plugin Portal. For now, you can use it via a [composite build](https://docs.gradle.org/current/userguide/composite_builds.html) or `publishToMavenLocal`.

**Using the plugins DSL** (recommended):

```kotlin
// root build.gradle.kts
plugins {
    id("io.github.antonioalmeida.dependency-lock-all") version "0.1.0"
}
```

**Using legacy plugin application:**

```kotlin
// root build.gradle.kts
buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath("io.github.antonioalmeida:dependency-lock-all:0.1.0")
    }
}

apply(plugin = "io.github.antonioalmeida.dependency-lock-all")
```

The plugin must be applied to the root project only.

## Usage

Write (or update) lock files across all projects:

```bash
./gradlew writeDependencyLocks --write-locks
```

## How the plugin works

When applied to the root project, the plugin:

1. Enables `dependencyLocking.lockAllConfigurations()` on every project (root + subprojects)
1. Registers a `writeDependencyLocks` task that resolves all resolvable configurations across all projects

Gradle's `--write-locks` flag tells the dependency locking setup to write `gradle.lockfile` whenever a locked configuration is resolved. The task just triggers that resolution.

## Requirements

- Gradle 8.x+

## Building from source

```bash
./gradlew build
```

## License

MIT
