package net.twisterrob.gradle.internal.lint

import com.android.build.gradle.tasks.LintGlobalTask
import net.twisterrob.gradle.compat.filePropertyCompat
import net.twisterrob.gradle.compat.fileProviderCompat
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.TaskCollection

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
