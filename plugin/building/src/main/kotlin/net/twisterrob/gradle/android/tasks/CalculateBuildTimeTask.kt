package net.twisterrob.gradle.android.tasks

import net.twisterrob.gradle.android.addBuildConfigField
import net.twisterrob.gradle.android.intermediateRegularFile
import net.twisterrob.gradle.common.AGPVersions
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

abstract class CalculateBuildTimeTask : DefaultTask() {

	/**
	 * Default implementation returns a one-day precise time
	 * to minimize `compile*JavaWithJavac` rebuilds due to a single number change in BuildConfig.java.
	 *
	 * It can be overridden like this:
	 * `tasks.calculateBuildConfigBuildTime.configure { buildTime.set(System.currentTimeMillis()) }`
	 *
	 * @returns a long representing the UTC time of the build.
	 */
	@get:Input
	abstract val buildTime: Property<Long>

	@get:OutputFile
	abstract val buildTimeFile: RegularFileProperty

	init {
		description = "Calculates the build time for BuildConfig.java."
		// Not using a provider to prevent turning over midnight during build,
		// each build will have a single calculation.
		@Suppress("LeakingThis")
		buildTime.convention(OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS).toInstant().toEpochMilli())
		@Suppress("LeakingThis")
		buildTimeFile.convention(project.intermediateRegularFile("buildConfigDecorations/buildTime.txt"))
	}

	@TaskAction
	fun writeBuildTime() {
		buildTimeFile.writeText(buildTime.get().toString())
	}

	companion object {

		internal fun TaskProvider<CalculateBuildTimeTask>.addBuildConfigFields(project: Project) {
			if (AGPVersions.CLASSPATH < AGPVersions.v41x) get().writeBuildTime()

			val buildTimeField = this
				.flatMap(CalculateBuildTimeTask::buildTimeFile)
				.map { file ->
					val buildTime = file.asFile.readText().toLong()
					val formattedBuildTime = DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(buildTime))
					"new java.util.Date(${buildTime}L) /* ${formattedBuildTime} */"
				}
			project.addBuildConfigField("BUILD_TIME", "java.util.Date", buildTimeField)
		}
	}
}
