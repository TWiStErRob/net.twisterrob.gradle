package net.twisterrob.gradle.common

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.SourceKind
import com.android.builder.model.AndroidProject.FD_GENERATED
import org.gradle.api.Action
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.reporting.ConfigurableReport
import org.gradle.api.reporting.ReportContainer
import org.gradle.api.reporting.Reporting
import org.gradle.api.reporting.ReportingExtension
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.VerificationTask
import java.io.File

open class VariantTaskCreator<T>(
		private val project: Project,
		private val baseName: String,
		private val pluginName: String,
		private val taskClass: Class<T>
) where
T : SourceTask,
T : Reporting<out ReportContainer<out ConfigurableReport>>,
T : TargetChecker,
T : VerificationTask {

	private lateinit var eachTask: Task

	fun applyTo(variants: DomainObjectSet<out BaseVariant>) {
		createGlobalTask()
		variants.all(this::createTaskForVariant)
		project.afterEvaluate {
			project.tasks.create("${baseName}All", taskClass, variantsConfig(variants))
		}
	}

	open fun variantsConfig(variants: Collection<BaseVariant>) = DefaultVariantsTaskConfig(taskConfigurator(), variants)

	open fun variantConfig(variant: BaseVariant) = DefaultVariantTaskConfig(taskConfigurator(), variant)

	open fun taskConfigurator() = DefaultTaskConfig()

	private fun createGlobalTask() {
		val globalTaskName = "${baseName}Each"
		if (project.tasks.findByName(globalTaskName) != null) {
			return@createGlobalTask
		}
		project.plugins.apply(pluginName)
		eachTask = project.tasks.create(globalTaskName) { task: Task ->
			task.group = JavaBasePlugin.VERIFICATION_GROUP
			task.description = "Run ${baseName} on each variant separately"
		}
	}

	private fun createTaskForVariant(variant: BaseVariant) {
		val taskName = "${baseName}${variant.name.capitalize()}"
		eachTask.dependsOn(project.tasks.create(taskName, taskClass, variantConfig(variant)))
	}

	open inner class DefaultVariantsTaskConfig(
			private val configurator: DefaultTaskConfig,
			private val variants: Collection<BaseVariant>
	) : Action<T> {

		override fun execute(task: T) {
			task.description = "Run ${baseName} batched on variants: ${variants.joinToString(", ", transform = BaseVariant::getName)}"
			task.checkTargetName = ALL_VARIANTS_NAME
			configurator.setupConfigLocations(task)
			configurator.setupSources(task, variants)
			configurator.setupReports(task)
		}
	}

	open inner class DefaultVariantTaskConfig(
			private val configurator: DefaultTaskConfig,
			private val variant: BaseVariant
	) : Action<T> {

		override fun execute(task: T) {
			task.description = "Run ${baseName} on ${variant.name} variant"
			task.checkTargetName = variant.name
			configurator.setupConfigLocations(task)
			configurator.setupSources(task, listOf(variant))
			configurator.setupReports(task, variant.name)
		}
	}

	open inner class DefaultTaskConfig {

		open fun setupConfigLocations(task: T) {
		}

		/**
		 * Add source paths from Java project source folders and exclude code we don't have control over.
		 * It should be enough to add source folders as coming from Android plugin,
		 * but then [SourceTask.exclude] doesn't work as expected.
		 * For this reason the paths need to be relativized to the project root so exclusion patterns work naturally.
		 *
		 * @see <a href="https://github.com/gradle/gradle/issues/3994">gradle/gradle#3994</a>
		 */
		open fun setupSources(task: T, variants: Collection<BaseVariant>) {
			// TODO classpath
			val buildPath = task.project.buildDir.toPath()
			val projectPath = task.project.projectDir.toPath()
			if (!buildPath.startsWith(projectPath)) {
				task.logger.warn("Cannot set up ${baseName} source folders," +
						" because the build directory ${buildPath}" +
						" needs to be inside the project directory ${projectPath}.")
				return@setupSources
			}
			val relativeBuildPath = projectPath.relativize(buildPath)

			// start with the whole project
			task.source(projectPath)

			// TODO too soon? ( android { sourceSets { main { srcDir 'blah' } } } }; test it
			// include whatever needs to be included
			task.include(variants
					.flatMap { it.getSourceFolders(SourceKind.JAVA) }
					.map { tree ->
						// build relative path (e.g. src/main/java) and
						// append a trailing "/" for include to treat it as recursive
						projectPath.relativize(tree.dir.toPath()).toString() + File.separator
					})
			// TODO want this? Checkstyle config can filter if wants to, otherwise XMLs can be checked as well
			//task.include '**/*.java' 

			variants.onEach { variant ->
				// exclude generated code
				// "source" is hard-coded in VariantScopeImpl, e.g. getAidlSourceOutputDir
				// single-star represents r|buildConfig|aidl|rs|etc.
				// double-star is the package name
				task.exclude("${relativeBuildPath}/${FD_GENERATED}/source/*/${variant.name}/**/*.java")
			}
		}

		open fun setupReports(task: T, suffix: String? = null) {
			val fullSuffix = if (suffix != null) "-" + suffix else ""
			// stop the build only if user wanted this task, otherwise we'll gather the results at once for reporting
			task.ignoreFailures = !wasExplicitlyLaunched(task)
			// TODO too soon?
			// Groovy static compilation can't figure it out, so help with a cast
			val reporting = task.project.extensions.findByType(ReportingExtension::class.java)
			val reportsDir = reporting!!.baseDir
			with(task.reports) {
				with(getByName("xml")) {
					isEnabled = true
					destination = File(reportsDir, "${baseName}${fullSuffix}.xml")
				}
				with(getByName("html")) {
					isEnabled = true
					destination = File(reportsDir, "${baseName}${fullSuffix}.html")
				}
			}
		}
	}
}
