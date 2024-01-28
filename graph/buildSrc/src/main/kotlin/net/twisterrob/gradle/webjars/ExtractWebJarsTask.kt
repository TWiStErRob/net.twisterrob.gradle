package net.twisterrob.gradle.webjars

import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.CompileClasspath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.submit
import org.gradle.workers.WorkerExecutor
import java.io.File
import javax.inject.Inject

abstract class ExtractWebJarsTask @Inject constructor(
	private val files: FileSystemOperations,
	private val workers: WorkerExecutor,
) : DefaultTask() {

	@get:Internal("Tracked by [configurationFiles].")
	internal abstract val artifacts: MapProperty<ModuleComponentIdentifier, File>

	@get:CompileClasspath
	@get:SkipWhenEmpty
	internal abstract val configurationFiles: ConfigurableFileCollection

	@get:Input
	@get:Optional
	abstract val cleanFirst: Property<Boolean>

	@get:OutputDirectory
	abstract val outputDirectory: DirectoryProperty

	/**
	 * It's very convoluted to consume [Configuration] / [ResolvedArtifactResult] objects as [Task] input.
	 * This method encapuslates what's necessary to make it work. Usage:
	 * ```kotlin
	 * register<ExtractWebJarsTask>("extractWebJars") {
	 *     fromConfiguration(configurations.named("webjars"))
	 *     ...
	 * }
	 * ```
	 *
	 * See [docs](https://docs.gradle.org/8.1.1/userguide/incremental_build.html#ex-resolved-artifacts-as-task-input).
	 * Related [documentation issue](https://github.com/gradle/gradle/issues/25372).
	 */
	fun fromConfiguration(configuration: Provider<Configuration>) {
		configurationFiles.setFrom(configuration)
		artifacts = configuration.flatMap { it.incoming.artifacts.resolvedArtifacts }.map {
			it
				.associateBy({ it.id.componentIdentifier }, { it.file })
				.filterKeys { it is ModuleComponentIdentifier }
				.mapKeys { it.key as ModuleComponentIdentifier }
		}
	}

	@TaskAction
	fun extract() {
		if (cleanFirst.getOrElse(false)) {
			files.delete { delete(outputDirectory) }
		}
		val work = workers.noIsolation()
		artifacts.get().forEach { (id, file) ->
			work.submit(ExtractWebJarAction::class) {
				this.localWebJar = file
				this.artifactId = "${id.group}:${id.module}:${id.version}"
				this.outputDirectory = this@ExtractWebJarsTask.outputDirectory
			}
		}
		work.await()
	}
}
