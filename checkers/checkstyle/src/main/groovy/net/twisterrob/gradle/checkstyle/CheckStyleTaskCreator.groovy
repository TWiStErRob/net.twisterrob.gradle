package net.twisterrob.gradle.checkstyle

import com.android.build.gradle.api.BaseVariant
import net.twisterrob.gradle.common.VariantTaskCreator
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaBasePlugin

class CheckStyleTaskCreator implements VariantTaskCreator {

	private final Project project

	private Task checkstyleEach

	CheckStyleTaskCreator(Project project) {
		this.project = project
	}

	@Override
	void applyTo(DomainObjectSet<? extends BaseVariant> variants) {
		createGlobalTask()
		variants.all this.&configureCheckStyle
		project.afterEvaluate {
			project.tasks.create('checkstyleAll', CheckStyleTask,
					new CheckStyleVariantsTaskConfig(variants))
		}
	}

	def createGlobalTask() {
		if (project.tasks.findByName('checkstyleEach') != null) {
			return
		}
		project.apply plugin: 'checkstyle'
		checkstyleEach = project.tasks.create('checkstyleEach') {Task task ->
			task.group = JavaBasePlugin.VERIFICATION_GROUP
			task.description = 'Run checkstyle on each variant separately'
		}
	}

	def configureCheckStyle(BaseVariant variant) {
		def checkstyleName = "checkstyle${variant.name.capitalize()}"
		checkstyleEach.dependsOn project.tasks.create(checkstyleName, CheckStyleTask,
				new CheckStyleVariantTaskConfig(variant))
	}
}
