package net.twisterrob.gradle.checkstyle

import com.android.build.gradle.api.BaseVariant
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaBasePlugin

class CheckStyleTaskCreator {

	private final Project project

	private Task checkstyleAll

	CheckStyleTaskCreator(Project project) {
		this.project = project
	}

	def applyTo(DomainObjectSet<? extends BaseVariant> variants) {
		createGlobalTask()
		variants.all this.&configureCheckStyle
	}

	def createGlobalTask() {
		if (project.tasks.findByName('checkStyleAll') != null) {
			return
		}
		project.apply plugin: 'checkstyle'
		checkstyleAll = project.tasks.create('checkstyleAll') {Task task ->
			task.group = JavaBasePlugin.VERIFICATION_GROUP
			task.description = 'Run checkstyle on all variants'
		}
	}

	def configureCheckStyle(BaseVariant variant) {
		def checkstyleName = "checkstyle${variant.name.capitalize()}"
		checkstyleAll.dependsOn project.tasks.create(checkstyleName,
				CheckStyleTask, new CheckStyleTask.TaskConfig(variant))
	}
}
