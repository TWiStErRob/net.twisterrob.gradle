package net.twisterrob.gradle.checkstyle

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.SourceKind
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.reporting.CustomizableHtmlReport
import org.gradle.api.reporting.ReportingExtension

import static com.android.builder.model.AndroidProject.FD_GENERATED

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

			task.classpath = project.files()
			setupConfigLocations(task)
			setupSources(task, variant)
			setupReports(task)
		}
	}

	private void setupConfigLocations(Checkstyle task) {
		if (!task.configFile.exists()) {
			task.configFile = project.rootProject.file('config/checkstyle/checkstyle.xml')
		}
		task.configDir = project.provider {task.configFile.parentFile}
		task.configProperties += [
				checked_project_dir: project.projectDir, // TODO or rootDir?
		] as Map<String, Object>
	}

	/**
	 * Add source paths from Java project source folders and exclude code we don't have control over.
	 * It should be enough to add source folders as coming from Android plugin,
	 * but then {@link Checkstyle#exclude} doesn't work as expected.
	 * For this reason the paths need to be relativized to the project root so exclusion patterns work naturally.
	 *
	 * @see <a href="https://github.com/gradle/gradle/issues/3994">gradle/gradle#3994</a>
	 */
	static def setupSources(Checkstyle task, BaseVariant variant) {
		def buildPath = task.project.buildDir.toPath()
		def projectPath = task.project.projectDir.toPath()
		if (!buildPath.startsWith(projectPath)) {
			task.logger.warn "Cannot set up Checkstyle source folders," +
					" because the build directory ${buildPath}" +
					" needs to be inside the project directory ${projectPath}."
			return
		}
		def relativeBuildPath = projectPath.relativize(buildPath)

		// start with the whole project
		task.source projectPath

		// include whatever needs to be included
		task.include variant.getSourceFolders(SourceKind.JAVA).collect {
			// build relative path (e.g. src/main/java) and append a trailing "/" for include to treat it as recursive
			projectPath.relativize(it.dir.toPath()).toString() + File.separator
		}
		// TODO want this? Checkstyle config can filter if wants to, otherwise XMLs can be checked as well
		//task.include '**/*.java' 

		// exclude generated code
		// "source" is hard-coded in VariantScopeImpl, e.g. getAidlSourceOutputDir
		// single-star represents r|buildConfig|aidl|rs|etc.
		// double-star is the package name
		task.exclude "${relativeBuildPath}/${FD_GENERATED}/source/*/${variant.name}/**/*.java"
		task.doFirst {
			println task.getSource().files.join("\n")
		}
	}

	static def setupReports(Checkstyle task) {
		task.reports.with {
			def reportsDir = task.project.extensions.findByType(ReportingExtension).baseDir
			xml.with {
				enabled = true
				setDestination(new File(reportsDir, 'checkstyle.xml'))
			}
			((CustomizableHtmlReport)html).with {
				enabled = true
				setDestination(new File(reportsDir, 'checkstyle.html'))
				def xsl = task.project.rootProject.file('config/checkstyle/checkstyle-html.xsl')
				if (xsl.exists()) {
					stylesheet = task.project.resources.text.fromFile(xsl)
				}
				return it
			}
		}
	}
}
