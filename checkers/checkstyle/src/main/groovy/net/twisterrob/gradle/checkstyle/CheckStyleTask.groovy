package net.twisterrob.gradle.checkstyle

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.SourceKind
import net.twisterrob.gradle.common.TargetChecker
import net.twisterrob.gradle.common.Utils
import org.gradle.api.Action
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.reporting.CustomizableHtmlReport
import org.gradle.api.reporting.ReportingExtension

import static com.android.builder.model.AndroidProject.FD_GENERATED

class CheckStyleTask extends Checkstyle implements TargetChecker {

	CheckStyleTask() {
		group = JavaBasePlugin.VERIFICATION_GROUP
		classpath = project.files()
		showViolations = false

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

	// cannot have the correct Generic arguments: https://issues.apache.org/jira/browse/GROOVY-8439
	static abstract class TaskConfig implements Action<CheckStyleTask> {

		static def setupConfigLocations(CheckStyleTask task) {
			if (!task.configFile.exists()) {
				def rootConfig = task.project.rootProject.file('config/checkstyle/checkstyle.xml')
				if (!rootConfig.exists()) {
					task.logger.warn """\
						While configuring ${task} no configuration found at:
							${rootConfig}
							${task.configFile}
					""".stripIndent()
				}
				task.configFile = rootConfig
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
		static def setupSources(CheckStyleTask task, Collection<? extends BaseVariant> variants) {
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
			task.include variants
					.collectMany {it.getSourceFolders(SourceKind.JAVA)}
					.collect {ConfigurableFileTree tree ->
				// build relative path (e.g. src/main/java) and
				// append a trailing "/" for include to treat it as recursive
				projectPath.relativize(tree.dir.toPath()).toString() + File.separator
			}
			// TODO want this? Checkstyle config can filter if wants to, otherwise XMLs can be checked as well
			//task.include '**/*.java' 

			variants.each {BaseVariant variant ->
				// exclude generated code
				// "source" is hard-coded in VariantScopeImpl, e.g. getAidlSourceOutputDir
				// single-star represents r|buildConfig|aidl|rs|etc.
				// double-star is the package name
				task.exclude "${relativeBuildPath}/${FD_GENERATED}/source/*/${variant.name}/**/*.java"
			}
		}

		static def setupReports(CheckStyleTask task, String suffix = null) {
			suffix = suffix != null? "-" + suffix : ""
			// stop the build only if user wanted this task, otherwise we'll gather the results at once for reporting
			task.ignoreFailures = Utils.wasExplicitlyLaunched(task)
			// TODO too soon?
			// Groovy static compilation can't figure it out, so help with a cast
			def reporting = task.project.extensions.findByType(ReportingExtension) as ReportingExtension
			File reportsDir = reporting.baseDir
			task.reports.with {
				xml.with {
					setEnabled true
					setDestination(new File(reportsDir, "checkstyle${suffix}.xml"))
				}
				((CustomizableHtmlReport)html).with {
					setEnabled true
					setDestination(new File(reportsDir, "checkstyle${suffix}.html"))
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
