package net.twisterrob.gradle.checkstyle

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.SourceKind
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.reporting.CustomizableHtmlReport

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
		project.tasks.create(name: "checkstyle${variant.name.capitalize()}", type: Checkstyle) {Checkstyle task ->
			task.group = JavaBasePlugin.VERIFICATION_GROUP
			task.description = "Run checkstyle on ${variant.name} variant"
			checkstyleAll.dependsOn task

			task.configDir = project.provider {task.configFile.parentFile}
			task.configProperties += [
					checked_project_dir: project.projectDir, // TODO or rootDir?
			] as Map<String, Object>
			task.source variant.getSourceFolders(SourceKind.JAVA)
//			task.include '**/*.java'
//			task.exclude '**/gen/**'

			task.classpath = project.files()
			task.showViolations = true

			task.reports.with {
				xml.with {
					enabled = true
				}
				((CustomizableHtmlReport)html).with {
					enabled = true
					setDestination(new File(project.buildDir, 'reports/checkstyle.html'))
					def xsl = project.rootProject.file('config/checkstyle/checkstyle-html.xsl')
					if (xsl.exists()) {
						stylesheet = project.resources.text.fromFile(xsl)
					}
					return it
				}
			}
		}
	}
}
