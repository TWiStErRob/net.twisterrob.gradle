package net.twisterrob.gradle.common

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.Variant
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.reporting.ConfigurableReport
import org.gradle.api.reporting.ReportContainer
import org.gradle.api.reporting.Reporting
import org.gradle.api.reporting.ReportingExtension
import org.gradle.api.reporting.SingleFileReport
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.VerificationTask
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.named
import java.util.Locale

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

	fun applyToJvm() {
		project.plugins.apply(pluginName)
		val eachTask = createGlobalTask()
		project.extensions.getByName<SourceSetContainer>("sourceSets").configureEach { sourceSet ->
			val taskName = sourceSet.getTaskName(baseName, null)
			eachTask.configure { it.dependsOn(taskName) }
		}
	}

	fun applyTo(androidComponents: AndroidComponentsExtension<*, *, *>) {
		project.plugins.apply(pluginName)
		val eachTask = createGlobalTask()
		// Probably false positive:
		// > Unsafe use of a nullable receiver of type DomainObjectSet<CapturedType(out [Suppress] BaseVariant)>
		@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
		androidComponents.onVariants { variant ->
			val variantTask = createTaskForVariant(variant)
			eachTask.configure { it.dependsOn(variantTask) }
		}
	}

	open fun variantConfig(variant: Variant): VariantTaskCreator<T>.DefaultVariantTaskConfig =
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

	private fun createTaskForVariant(variant: Variant): TaskProvider<T> {
		val taskName = "${baseName}${variant.name.replaceFirstChar { it.uppercase(Locale.ROOT) }}"
		return project.tasks.register(taskName, taskClass, variantConfig(variant))
	}

	open inner class DefaultVariantTaskConfig(
		private val configurator: DefaultTaskConfig,
		private val variant: Variant,
	) : Action<T> {

		override fun execute(task: T) {
			task.description = "Run ${baseName} on ${variant.name} variant"
			task.checkTargetName = variant.name
			configurator.setupConfigLocations(task)
			configurator.setupSources(task, variant)
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
		open fun setupSources(task: T, variant: Variant) {
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

			task.source( variant.sources.java?.static)
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
