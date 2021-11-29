package net.twisterrob.gradle.internal.lint

import com.android.build.gradle.tasks.LintGlobalTask
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskCollection
import org.gradle.util.GradleVersion
import java.io.File

val Project.lintGlobalTasks: TaskCollection<LintGlobalTask>
	get() = this.tasks.withType(LintGlobalTask::class.java)

fun TaskCollection<LintGlobalTask>.configureXmlReport() {
	this.configureEach {
		it.lintOptions.isAbortOnError = it.wasLaunchedExplicitly
		// make sure we have xml output, otherwise can't figure out if it failed
		it.lintOptions.xmlReport = true
	}
}

fun TaskCollection<LintGlobalTask>.collectXmlReport(reportDiscovered: (RegularFileProperty) -> Unit) {
	this.configureEach {
		reportDiscovered(it.xmlOutputProperty)
	}
}

private val LintGlobalTask.xmlOutputProperty: RegularFileProperty
	get() =
		project.objects
			.filePropertyCompat(this, false)
			.fileProviderCompat(this, project.provider { this.xmlOutput })

private val Task.wasLaunchedExplicitly: Boolean
	get() = path in project.gradle.startParameter.taskNames

/**
 * Gradle 4.3-6.9 compatible version of [ObjectFactory.fileProperty].
 * @param task is necessary to because historically this was [DefaultTask.newInputFile] or [DefaultTask.newOutputFile].
 *
 * @see DefaultTask.newInputFile
 * @see DefaultTask.newOutputFile
 * @see ObjectFactory.fileProperty
 */
fun ObjectFactory.filePropertyCompat(task: DefaultTask, isInput: Boolean): RegularFileProperty =
	when {
		GradleVersion.current().baseVersion < GradleVersion.version("5.0") -> {
			if (isInput) {
				@Suppress("DEPRECATION")
				task.newInputFile()
			} else {
				@Suppress("DEPRECATION")
				task.newOutputFile()
			}
		}
		else -> {
			// New in Gradle 5.0.
			this.fileProperty()
		}
	}

/**
 * Gradle 4.3-6.9 compatible version of [RegularFileProperty.fileProvider].
 *
 * @see RegularFileProperty.set
 * @see RegularFileProperty.fileProvider
 */
fun RegularFileProperty.fileProviderCompat(task: DefaultTask, file: Provider<File>): RegularFileProperty =
	when {
		GradleVersion.current().baseVersion < GradleVersion.version("6.0") -> {
			this.set(file.map { task.project.objects.filePropertyCompat(task, false).apply { set(it) }.get() })
			this
		}
		else -> {
			// New in Gradle 6.0, https://docs.gradle.org/6.0/release-notes.html#new-convenience-methods-for-bridging-between-a-regularfileproperty-or-directoryproperty-and-a-file.
			this.fileProvider(file)
		}
	}

/**
 * Polyfill as reflective call, as this method was...
 *  * [Added in Gradle 4.3](https://docs.gradle.org/4.3/release-notes.html#improvements-for-plugin-authors)
 *  * [Deprecated in Gradle 5.0](https://docs.gradle.org/5.0/release-notes.html#changes-to-incubating-factory-methods-for-creating-properties)
 *  * [Removed in Gradle 6.0](https://docs.gradle.org/6.0/userguide/upgrading_version_5.html#replaced_and_removed_apis)
 *
 * @see DefaultTask.newInputFileCompat
 */
@Deprecated(
	message = "Replaced with ObjectFactory.fileProperty().",
	replaceWith = ReplaceWith("project.objects.fileProperty()")
)
fun DefaultTask.newInputFile(): RegularFileProperty {
	val newInputFile = DefaultTask::class.java.getDeclaredMethod("newInputFile").apply {
		// protected to public as this extension function is static and external to DefaultTask.
		isAccessible = true
	}
	return newInputFile(this) as RegularFileProperty
}

/**
 * Polyfill as reflective call, as this method was...
 *  * [Added in Gradle 4.3](https://docs.gradle.org/4.3/release-notes.html#improvements-for-plugin-authors)
 *  * [Deprecated in Gradle 5.0](https://docs.gradle.org/5.0/release-notes.html#changes-to-incubating-factory-methods-for-creating-properties)
 *  * [Removed in Gradle 6.0](https://docs.gradle.org/6.0/userguide/upgrading_version_5.html#replaced_and_removed_apis)
 *
 * @see DefaultTask.newOutputFileCompat
 */
@Deprecated(
	message = "Replaced with ObjectFactory.fileProperty().",
	replaceWith = ReplaceWith("project.objects.fileProperty()")
)
fun DefaultTask.newOutputFile(): RegularFileProperty {
	val newOutputFile = DefaultTask::class.java.getDeclaredMethod("newOutputFile").apply {
		// protected to public as this extension function is static and external to DefaultTask.
		isAccessible = true
	}
	return newOutputFile(this) as RegularFileProperty
}
