package net.twisterrob.gradle.checkstyle

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.SourceKind
import org.gradle.api.Action
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.reporting.CustomizableHtmlReport
import org.gradle.api.reporting.ReportingExtension

import static com.android.builder.model.AndroidProject.FD_GENERATED

class CheckStyleTask extends Checkstyle {

	private Object variant

	CheckStyleTask() {
		group = JavaBasePlugin.VERIFICATION_GROUP
		classpath = project.files()

		setupProperties()
	}

	def setupProperties() {
		// partially based on https://github.com/jshiell/checkstyle-idea#eclipse-cs-variable-support
		configProperties += [
				basedir      : project.projectDir, // TODO or rootDir?
				project_loc  : project.rootDir,
				workspace_loc: project.rootDir,
				//config_loc: configFile.parentFile // set via setConfigDir
				//samedir: use config_loc instead (until I figure out how to do doFirst properly)
		] as Map<String, Object>
		setConfigDir project.provider {configFile.parentFile}
	}

	Object getVariant() {
		return variant as BaseVariant
	}

	private void setVariant(Object variant) {
		this.variant = variant
	}

	static class TaskConfig implements Action<CheckStyleTask> {

		private final BaseVariant variant

		TaskConfig(BaseVariant variant) {
			this.variant = variant
		}

		@Override
		void execute(CheckStyleTask task) {
			task.description = "Run checkstyle on ${variant.name} variant"
			task.variant = variant
			setupConfigLocations(task)
			setupSources(task, variant)
			setupReports(task)
		}

		static def setupConfigLocations(CheckStyleTask task) {
			if (!task.configFile.exists()) {
				task.configFile = task.project.rootProject.file('config/checkstyle/checkstyle.xml')
			}
		}

		/**
		 * Add source paths from Java project source folders and exclude code we don't have control over.
		 * It should be enough to add source folders as coming from Android plugin,
		 * but then {@link Checkstyle#exclude} doesn't work as expected.
		 * For this reason the paths need to be relativized to the project root so exclusion patterns work naturally.
		 *
		 * @see <a href="https://github.com/gradle/gradle/issues/3994">gradle/gradle#3994</a>
		 */
		static def setupSources(CheckStyleTask task, BaseVariant variant) {
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

			// TODO too soon? ( android { sourceSets { main { srcDir 'blah' } } } }; test it
			// include whatever needs to be included
			task.include variant.getSourceFolders(SourceKind.JAVA).collect {
				// build relative path (e.g. src/main/java) and
				// append a trailing "/" for include to treat it as recursive
				projectPath.relativize(it.dir.toPath()).toString() + File.separator
			}
			// TODO want this? Checkstyle config can filter if wants to, otherwise XMLs can be checked as well
			//task.include '**/*.java' 

			// exclude generated code
			// "source" is hard-coded in VariantScopeImpl, e.g. getAidlSourceOutputDir
			// single-star represents r|buildConfig|aidl|rs|etc.
			// double-star is the package name
			task.exclude "${relativeBuildPath}/${FD_GENERATED}/source/*/${variant.name}/**/*.java"
		}

		static def setupReports(CheckStyleTask task) {
			// don't stop the build, so we have all the results at once for reporting
			// if this is the only task being requested by the user, fail the build on failures
			task.ignoreFailures = task.project.gradle.startParameter.taskNames != [ task.path ]
			// TODO too soon?
			def reportsDir = task.project.extensions.findByType(ReportingExtension).baseDir
			task.reports.with {
				xml.with {
					setEnabled true
					setDestination(new File(reportsDir, 'checkstyle.xml'))
				}
				((CustomizableHtmlReport)html).with {
					setEnabled true
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
}
