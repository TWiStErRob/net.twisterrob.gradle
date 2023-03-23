package net.twisterrob.gradle.internal.lint

import com.android.build.gradle.tasks.LintGlobalTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.TaskCollection

val Project.lintGlobalTasks: TaskCollection<LintGlobalTask>
	get() = this.tasks.withType(LintGlobalTask::class.java)

fun TaskCollection<LintGlobalTask>.configureXmlReport() {
	this.configureEach { task ->
		task.lintOptions.isAbortOnError = task.wasLaunchedExplicitly
		// make sure we have xml output, otherwise can't figure out if it failed
		task.lintOptions.xmlReport = true
	}
}

fun TaskCollection<LintGlobalTask>.collectXmlReport(reportDiscovered: (RegularFileProperty) -> Unit) {
	this.configureEach { task ->
		reportDiscovered(task.xmlOutputProperty)
	}
}

private val LintGlobalTask.xmlOutputProperty: RegularFileProperty
	get() =
		project.objects
			.fileProperty()
			.fileProvider(project.provider { this.xmlOutput })

private val Task.wasLaunchedExplicitly: Boolean
	get() = path in project.gradle.startParameter.taskNames
