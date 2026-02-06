plugins {
    `kotlin-dsl`
    id("com.gradle.plugin-publish") version "2.0.0"
}

group = property("group") as String
version = property("version") as String

repositories {
    mavenCentral()
}

gradlePlugin {
    website = "https://github.com/antonioalmeida/gradle-multi-project-dependency-locking"
    vcsUrl = "https://github.com/antonioalmeida/gradle-multi-project-dependency-locking"

    plugins {
        create("dependencyLockAll") {
            id = "io.github.antonioalmeida.dependency-lock-all"
            displayName = "Dependency Lock All"
            description = "Configures dependency locking across all projects and provides a single task to write lock files"
            tags = listOf("dependencies", "locking", "dependency-locking")
            implementationClass = "io.github.antonioalmeida.lockall.DependencyLockAllPlugin"
        }
    }
}

testing {
    suites {
        val functionalTest by registering(JvmTestSuite::class) {
            useJUnitJupiter()

            dependencies {
                implementation(project())
            }

            targets {
                all {
                    testTask.configure {
                        shouldRunAfter(tasks.named("test"))
                    }
                }
            }
        }
    }
}

gradlePlugin.testSourceSets.add(sourceSets["functionalTest"])

tasks.named("check") {
    dependsOn(testing.suites.named("functionalTest"))
}
