package net.twisterrob.gradle.android.tasks

import com.android.build.gradle.BaseExtension
import net.twisterrob.gradle.android.asBuildConfigField
import net.twisterrob.gradle.android.intermediateRegularFile
import net.twisterrob.gradle.android.onVariantProperties
import net.twisterrob.gradle.writeText
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

private val DAY = TimeUnit.DAYS.toMillis(1)

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
	var getBuildTime = { OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS).toInstant().toEpochMilli() }

	@get:OutputFile
	val buildTimeFile: RegularFileProperty = intermediateRegularFile("buildConfigDecorations/buildTime.txt")

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

		fun TaskProvider<CalculateBuildTimeTask>.addBuildConfigFields(android: BaseExtension) {
			val buildTimeField = flatMap(CalculateBuildTimeTask::buildTimeFile).map {
				it.asBuildConfigField("java.util.Date") { date ->
					fun dateFormat(date: Long): String =
						DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(date))

					val buildTime = date.toLong()
					"new java.util.Date(${buildTime}L) /* ${dateFormat(buildTime)} */"
				}
			}
			android.onVariantProperties {
				buildConfigFields.put("BUILD_TIME", buildTimeField)
			}
		}
	}
}
