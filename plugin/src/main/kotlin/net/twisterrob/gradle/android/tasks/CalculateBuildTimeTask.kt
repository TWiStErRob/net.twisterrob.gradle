package net.twisterrob.gradle.android.tasks

import net.twisterrob.gradle.android.addBuildConfigField
import net.twisterrob.gradle.android.intermediateRegularFile
import net.twisterrob.gradle.common.AGPVersions
import net.twisterrob.gradle.writeText
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.UntrackedTask
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@UntrackedTask(because = "Input of this task is the current time.")
open class CalculateBuildTimeTask : DefaultTask() {

	/**
	 * Default implementation returns a one-day precise time
	 * to minimize `compile*JavaWithJavac` rebuilds due to a single number change in BuildConfig.java.
	 *
	 * It can be overridden like this:
	 * `tasks.calculateBuildConfigBuildTime.configure { getBuildTime = { System.currentTimeMillis() }}`
	 *
	 * @returns a long representing the UTC time of the build.
	 */
	@Input
	var getBuildTime: () -> Long =
		{ OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS).toInstant().toEpochMilli() }

	@get:OutputFile
	val buildTimeFile: RegularFileProperty =
		intermediateRegularFile("buildConfigDecorations/buildTime.txt")

	init {
		description = "Calculates the build time for BuildConfig.java."
		outputs.upToDateWhen { false }
	}

	@TaskAction
	fun writeBuildTime() {
		val buildTime = getBuildTime()
		buildTimeFile.writeText(buildTime.toString())
	}

	companion object {

		fun TaskProvider<CalculateBuildTimeTask>.addBuildConfigFields(project: Project) {
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
