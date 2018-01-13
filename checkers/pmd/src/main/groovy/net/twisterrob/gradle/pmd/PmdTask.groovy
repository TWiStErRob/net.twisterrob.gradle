package net.twisterrob.gradle.pmd

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.SourceKind
import net.twisterrob.gradle.common.TargetChecker
import org.gradle.api.Action
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.plugins.quality.Pmd
import org.gradle.api.reporting.ReportingExtension

import static com.android.builder.model.AndroidProject.FD_GENERATED

class PmdTask extends Pmd implements TargetChecker {

	PmdTask() {
		group = JavaBasePlugin.VERIFICATION_GROUP
		classpath = project.files()

		setupProperties()
	}

	def setupProperties() {
		// TODO expose similar properties to CS, for <rule message and path substitution
	}

	// cannot have the correct Generic arguments: https://issues.apache.org/jira/browse/GROOVY-8439
	static abstract class TaskConfig implements Action<PmdTask> {

		static def setupConfigLocations(PmdTask task) {
			task.ruleSets = [ ] // default is java-basic
			File rootConfig = task.project.rootProject.file('config/pmd/pmd.xml')
			File subConfig = task.project.file('config/pmd/pmd.xml')
			File config = [ subConfig, rootConfig ].grep {File file -> file.exists()}.find()
			task.ruleSetFiles += task.project.files(config)
		}

		/**
		 * Add source paths from Java project source folders and exclude code we don't have control over.
		 * It should be enough to add source folders as coming from Android plugin,
		 * but then {@link Checkstyle#exclude} doesn't work as expected.
		 * For this reason the paths need to be relativized to the project root so exclusion patterns work naturally.
		 *
		 * @see <a href="https://github.com/gradle/gradle/issues/3994">gradle/gradle#3994</a>
		 */
		static def setupSources(PmdTask task, Collection<? extends BaseVariant> variants) {
			// TODO classpath
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

		static def setupReports(PmdTask task, String suffix = null) {
			suffix = suffix != null? "-" + suffix : ""
			// don't stop the build, so we have all the results at once for reporting
			// if this is the only task being requested by the user, fail the build on failures
			task.ignoreFailures = task.project.gradle.startParameter.taskNames != [ task.path ]
			// TODO too soon?
			// Groovy static compilation can't figure it out, so help with a cast
			def reporting = task.project.extensions.findByType(ReportingExtension) as ReportingExtension
			File reportsDir = reporting.baseDir
			task.reports.with {
				xml.with {
					setEnabled true
					setDestination(new File(reportsDir, "pmd${suffix}.xml"))
				}
				html.with {
					setEnabled true
					setDestination(new File(reportsDir, "pmd${suffix}.html"))
				}
			}
		}
	}
}
