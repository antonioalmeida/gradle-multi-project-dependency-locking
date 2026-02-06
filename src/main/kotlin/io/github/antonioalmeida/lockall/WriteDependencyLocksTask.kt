package io.github.antonioalmeida.lockall

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class WriteDependencyLocksTask : DefaultTask() {

    @TaskAction
    fun resolveAll() {
        project.rootProject.allprojects.forEach { subproject ->
            logger.lifecycle("Resolving configurations for project '${subproject.path}'")

            subproject.configurations
                .filter { it.isCanBeResolved }
                .forEach { configuration ->
                    try {
                        logger.info("  Resolving configuration '${configuration.name}'")
                        configuration.resolve()
                    } catch (e: Exception) {
                        logger.info(
                            "  Skipping configuration '${configuration.name}': ${e.message}"
                        )
                    }
                }
        }
    }
}
