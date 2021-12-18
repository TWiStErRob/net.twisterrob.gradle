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
	val buildTimeFile: RegularFileProperty =
		intermediateRegularFile("buildConfigDecorations/buildTime.txt")

	init {
		description = "Calculates the build time for BuildConfig.java."
		// Not using a provider to prevent turning over midnight during build,
		// each build will have a single calculation.
		buildTime.convention(OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS).toInstant().toEpochMilli())
	}

	@TaskAction
	fun writeBuildTime() {
		buildTimeFile.writeText(buildTime.get().toString())
	}

	companion object {

		internal fun TaskProvider<CalculateBuildTimeTask>.addBuildConfigFields(project: Project) {
			if (AGPVersions.CLASSPATH < AGPVersions.v41x) get().writeBuildTime()

			val buildTimeField = this.flatMap(CalculateBuildTimeTask::buildTimeFile).map {
				fun dateFormat(date: Long): String =
					DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(date))

				val buildTime = it.asFile.readText().toLong()

				return@map "new java.util.Date(${buildTime}L) /* ${dateFormat(buildTime)} */"
			}
			project.addBuildConfigField("BUILD_TIME", "java.util.Date", buildTimeField)
		}
	}
}
