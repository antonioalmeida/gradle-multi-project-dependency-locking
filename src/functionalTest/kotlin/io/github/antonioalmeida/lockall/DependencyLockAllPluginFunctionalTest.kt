package io.github.antonioalmeida.lockall

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class DependencyLockAllPluginFunctionalTest {

    @TempDir
    lateinit var projectDir: File

    private val settingsFile by lazy { File(projectDir, "settings.gradle.kts") }
    private val buildFile by lazy { File(projectDir, "build.gradle.kts") }

    @BeforeEach
    fun setup() {
        settingsFile.writeText(
            """
            rootProject.name = "test-project"
            include("sub-a", "sub-b")
            """.trimIndent()
        )

        buildFile.writeText(
            """
            plugins {
                id("io.github.antonioalmeida.dependency-lock-all")
            }
            """.trimIndent()
        )

        File(projectDir, "sub-a").mkdir()
        File(projectDir, "sub-a/build.gradle.kts").writeText(
            """
            plugins {
                `java-library`
            }
            repositories {
                mavenCentral()
            }
            dependencies {
                implementation("com.google.guava:guava:33.0.0-jre")
            }
            """.trimIndent()
        )

        File(projectDir, "sub-b").mkdir()
        File(projectDir, "sub-b/build.gradle.kts").writeText(
            """
            plugins {
                `java-library`
            }
            repositories {
                mavenCentral()
            }
            dependencies {
                implementation("org.apache.commons:commons-lang3:3.14.0")
            }
            """.trimIndent()
        )
    }

    @Test
    fun `generates lock files for all subprojects`() {
        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments("writeDependencyLocks", "--write-locks")
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":writeDependencyLocks")?.outcome)

        val subALockFile = File(projectDir, "sub-a/gradle.lockfile")
        assertTrue(subALockFile.exists(), "sub-a/gradle.lockfile should exist")
        assertTrue(subALockFile.readText().contains("com.google.guava:guava:"), "Lock file should contain guava")

        val subBLockFile = File(projectDir, "sub-b/gradle.lockfile")
        assertTrue(subBLockFile.exists(), "sub-b/gradle.lockfile should exist")
        assertTrue(subBLockFile.readText().contains("org.apache.commons:commons-lang3:"), "Lock file should contain commons-lang3")
    }

    @Test
    fun `task has correct group`() {
        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments("tasks", "--group", "dependency locking")
            .build()

        assertTrue(result.output.contains("writeDependencyLocks"))
    }

    @Test
    fun `generates lock file for root project with dependencies`() {
        buildFile.writeText(
            """
            plugins {
                `java-library`
                id("io.github.antonioalmeida.dependency-lock-all")
            }
            repositories {
                mavenCentral()
            }
            dependencies {
                implementation("com.google.guava:guava:33.0.0-jre")
            }
            """.trimIndent()
        )

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments("writeDependencyLocks", "--write-locks")
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":writeDependencyLocks")?.outcome)

        val rootLockFile = File(projectDir, "gradle.lockfile")
        assertTrue(rootLockFile.exists(), "Root gradle.lockfile should exist")
        assertTrue(rootLockFile.readText().contains("com.google.guava:guava:"), "Root lock file should contain guava")
    }
}
