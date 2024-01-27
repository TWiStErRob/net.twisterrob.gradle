package net.twisterrob.gradle.common

import com.android.SdkConstants.FD_GENERATED
import org.gradle.api.Action
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.reporting.ConfigurableReport
import org.gradle.api.reporting.ReportContainer
import org.gradle.api.reporting.Reporting
import org.gradle.api.reporting.ReportingExtension
import org.gradle.api.reporting.SingleFileReport
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.VerificationTask
import org.gradle.kotlin.dsl.named
import java.io.File

@Suppress("DEPRECATION" /* AGP 7.0 */)
private typealias BaseVariant = com.android.build.gradle.api.BaseVariant

open class VariantTaskCreator<T>(
	private val project: Project,
	private val baseName: String,
	private val pluginName: String,
	private val taskClass: Class<T>,
	private val extensionClass: Class<out BaseQualityExtension<T>>
) where
T : SourceTask,
T : Reporting<out ReportContainer<out ConfigurableReport>>,
T : TargetChecker,
T : VerificationTask {

	private val checkerExtension: BaseQualityExtension<T>
		get() {
			val quality = project.extensions.getByName("quality") as ExtensionAware
			return quality.extensions.getByType(extensionClass)
		}

	fun applyTo(
		variants: DomainObjectSet<out @Suppress("TYPEALIAS_EXPANSION_DEPRECATION" /* AGP 7.0 */) BaseVariant>
	) {
		project.plugins.apply(pluginName)
		val eachTask = createGlobalTask()
		variants.all { createTaskForVariant(it, eachTask) }
		project.afterEvaluate {
			project.tasks.register("${baseName}All", taskClass, variantsConfig(variants))
		}
	}

	open fun variantsConfig(
		variants: Collection<@Suppress("TYPEALIAS_EXPANSION_DEPRECATION" /* AGP 7.0 */) BaseVariant>
	): VariantTaskCreator<T>.DefaultVariantsTaskConfig =
		DefaultVariantsTaskConfig(taskConfigurator(), variants)

	open fun variantConfig(
		variant: @Suppress("TYPEALIAS_EXPANSION_DEPRECATION" /* AGP 7.0 */) BaseVariant
	): VariantTaskCreator<T>.DefaultVariantTaskConfig =
		DefaultVariantTaskConfig(taskConfigurator(), variant)

	open fun taskConfigurator(): VariantTaskCreator<T>.DefaultTaskConfig =
		DefaultTaskConfig()

	private fun createGlobalTask(): TaskProvider<Task> {
		val globalTaskName = "${baseName}Each"
		if (globalTaskName in project.tasks.names) {
			return project.tasks.named(globalTaskName)
		}
		return project.tasks.register(globalTaskName) { task: Task ->
			task.group = JavaBasePlugin.VERIFICATION_GROUP
			task.description = "Run ${baseName} on each variant separately"
		}
	}

	private fun createTaskForVariant(
		variant: @Suppress("TYPEALIAS_EXPANSION_DEPRECATION" /* AGP 7.0 */) BaseVariant,
		eachTask: TaskProvider<*>,
	) {
		val taskName = "${baseName}${variant.name.capitalize()}"
		val variantTask = project.tasks.register(taskName, taskClass, variantConfig(variant))
		eachTask.configure { it.dependsOn(variantTask) }
	}

	open inner class DefaultVariantsTaskConfig(
		private val configurator: DefaultTaskConfig,
		private val variants: Collection<@Suppress("TYPEALIAS_EXPANSION_DEPRECATION" /* AGP 7.0 */) BaseVariant>
	) : Action<T> {

		override fun execute(task: T) {
			val variantNames = variants.joinToString(", ") { it.name }
			task.description = "Run ${baseName} batched on variants: ${variantNames}"
			task.checkTargetName = ALL_VARIANTS_NAME
			configurator.setupConfigLocations(task)
			configurator.setupSources(task, variants)
			configurator.setupReports(task)
			checkerExtension.taskConfigurator.execute(TaskConfigurator(task))
		}
	}

	open inner class DefaultVariantTaskConfig(
		private val configurator: DefaultTaskConfig,
		private val variant: @Suppress("TYPEALIAS_EXPANSION_DEPRECATION" /* AGP 7.0 */) BaseVariant
	) : Action<T> {

		override fun execute(task: T) {
			task.description = "Run ${baseName} on ${variant.name} variant"
			task.checkTargetName = variant.name
			configurator.setupConfigLocations(task)
			configurator.setupSources(task, listOf(variant))
			configurator.setupReports(task, variant.name)
			checkerExtension.taskConfigurator.execute(TaskConfigurator(task))
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
		open fun setupSources(
			task: T,
			variants: Collection<@Suppress("TYPEALIAS_EXPANSION_DEPRECATION" /* AGP 7.0 */) BaseVariant>
		) {
			// TODO classpath
			@Suppress("detekt.MaxChainedCallsOnSameLine")
			val buildPath = task.project.layout.buildDirectory.get().asFile.toPath()
			@Suppress("detekt.MaxChainedCallsOnSameLine")
			val projectPath = task.project.layout.projectDirectory.asFile.toPath()
			if (!buildPath.startsWith(projectPath)) {
				task.logger.warn(
					"""
						|Cannot set up ${baseName} source folders,
						|	because the build directory ${buildPath}
						|	needs to be inside the project directory ${projectPath}.
					""".trimMargin().replace("""\r?\n\t*""".toRegex(), " ")
				)
				return
			}
			val relativeBuildPath = projectPath.relativize(buildPath)

			// start with the whole project
			task.source(projectPath)

			// include whatever needs to be included
			@Suppress("DEPRECATION" /* AGP 7.0 */)
			val java = com.android.build.gradle.api.SourceKind.JAVA
			val javaFolders =
				variants
					.flatMap { it.getSourceFolders(java) }
					.map { tree ->
						// build relative path (e.g. src/main/java) and
						// append a trailing "/" for include to treat it as recursive
						projectPath.relativize(tree.dir.toPath()).toString() + File.separator
					}
			task.include(javaFolders)

			variants.forEach { variant ->
				// exclude generated code
				// "source" is hard-coded in VariantScopeImpl, e.g. getAidlSourceOutputDir
				// single-star represents r|buildConfig|aidl|rs|etc.
				// double-star is the package name
				task.exclude("${relativeBuildPath}/${FD_GENERATED}/source/*/${variant.name}/**/*.java")
			}
		}

		open fun setupReports(task: T, suffix: String? = null) {
			val fullSuffix = if (suffix != null) "-${suffix}" else ""
			// stop the build only if user wanted this task, otherwise we'll gather the results at once for reporting
			task.ignoreFailures = !task.wasLaunchedOnly
			// TODO too soon?
			val reporting: ReportingExtension = task.project.extensions.findByType(ReportingExtension::class.java)
				?: error("Cannot find reporting extension, did you apply `reporting` plugin?")
			val reportsDir = reporting.baseDirectory
			with(task.reports) {
				named<SingleFileReport>("xml").configure { xml ->
					xml.required.set(true)
					xml.outputLocation.set(reportsDir.file("${baseName}${fullSuffix}.xml"))
				}
				named<SingleFileReport>("html").configure { html ->
					html.required.set(true)
					html.outputLocation.set(reportsDir.file("${baseName}${fullSuffix}.html"))
				}
			}
		}
	}
}
