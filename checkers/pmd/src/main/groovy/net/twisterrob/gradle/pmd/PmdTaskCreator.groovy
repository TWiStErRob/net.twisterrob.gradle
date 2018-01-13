package net.twisterrob.gradle.pmd

import com.android.build.gradle.api.BaseVariant
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaBasePlugin

// TODO dedupe these classes
class PmdTaskCreator {

	private final Project project

	private Task pmdEach

	PmdTaskCreator(Project project) {
		this.project = project
	}

	def applyTo(DomainObjectSet<? extends BaseVariant> variants) {
		createGlobalTask()
		variants.all this.&configureCheckStyle
		project.afterEvaluate {
			project.tasks.create('pmdAll', PmdTask,
					new PmdVariantsTaskConfig(variants))
		}
	}

	def createGlobalTask() {
		if (project.tasks.findByName('pmdEach') != null) {
			return
		}
		project.apply plugin: 'pmd'
		pmdEach = project.tasks.create('pmdEach') {Task task ->
			task.group = JavaBasePlugin.VERIFICATION_GROUP
			task.description = 'Run pmd on each variant separately'
		}
	}

	def configureCheckStyle(BaseVariant variant) {
		def pmdName = "pmd${variant.name.capitalize()}"
		pmdEach.dependsOn project.tasks.create(pmdName, PmdTask,
				new PmdVariantTaskConfig(variant))
	}
}
