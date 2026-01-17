package net.twisterrob.gradle.quality.tasks

import net.twisterrob.gradle.common.grouper.Grouper
import net.twisterrob.gradle.common.nullSafeSum
import net.twisterrob.gradle.quality.Violation
import net.twisterrob.gradle.quality.Violations
import net.twisterrob.gradle.quality.gather.LintReportGatherer
import net.twisterrob.gradle.quality.gather.QualityTaskReportGatherer
import net.twisterrob.gradle.quality.gather.TaskReportGatherer
import net.twisterrob.gradle.quality.report.html.deduplicate
import net.twisterrob.gradle.quality.violations.RuleCategoryParser
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.plugins.quality.Pmd
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask
import se.bjurr.violations.lib.model.SEVERITY
import se.bjurr.violations.lib.reports.Parser
import java.io.File
import java.io.Serializable
import kotlin.jvm.java

@UntrackedTask(because = "Abstract super-class, not to be instantiated directly.")
abstract class BaseViolationsTask : DefaultTask() {

	@get:Input
	internal abstract val tasks: ListProperty<Result>

	@get:InputFiles
	@get:PathSensitive(PathSensitivity.ABSOLUTE)
	internal abstract val reports: ConfigurableFileCollection

	init {
		this.group = JavaBasePlugin.VERIFICATION_GROUP
		reports.from(project.provider {
			// Make sure external reports are involved in UP-TO-DATE checks.
			project.files(
				project.allprojects.flatMap { subproject ->
					GATHERERS.flatMap { gatherer ->
						gatherer.allTasksFrom(subproject).map { task ->
							gatherer.getParsableReportLocation(task)
						}
					}
				}
			)
		})

		// Make sure inputs are available when running validation, but don't execute (depend on) reports.
		project.allprojects.forEach { subproject: Project ->
			GATHERERS.forEach { gatherer ->
				gatherer.allTasksFrom(subproject).configureEach { reportTask ->
					mustRunAfter(reportTask)
				}
			}
		}

		// Make all information available to the task.
		tasks.convention(project.provider {
			project.allprojects.flatMap { subproject ->
				GATHERERS.flatMap { gatherer ->
					gatherer.allTasksFrom(subproject).map { task ->
						Result(
							subproject = Result.Project(
								name = subproject.name,
								path = subproject.path,
								rootDir = subproject.rootDir,
								projectDir = subproject.projectDir,
							),
							gatherer = gatherer,
							task = task.path,
							displayName = gatherer.getDisplayName(task),
							gathererName = gatherer.getName(task),
							parsableReportLocation = gatherer.getParsableReportLocation(task),
							humanReportLocation = gatherer.getHumanReportLocation(task),
						)
					}
				}
			}
		})
	}

	protected abstract fun processViolations(violations: Grouper.Start<Violations>)

	@TaskAction
	fun validateViolations() {
		val ruleCategoryParser = RuleCategoryParser()
		val results = tasks.get().map { result ->
			if (!result.parsableReportLocation.exists()) {
				logger.info(
					"Missing report for task '{}' (probably wasn't executed yet after clean): {}",
					result.task,
					result.parsableReportLocation,
				)
			}
			Violations(
				parser = result.displayName,
				module = result.subproject.path,
				variant = result.gathererName,
				result = result.parsableReportLocation,
				report = result.humanReportLocation,
				violations = result.gatherer.getViolations(result.parsableReportLocation)?.map { violation ->
					Violation(
						rule = ruleCategoryParser.rule(violation),
						category = ruleCategoryParser.category(violation),
						severity = when (violation.severity!!) {
							SEVERITY.INFO -> Violation.Severity.INFO
							SEVERITY.WARN -> Violation.Severity.WARNING
							SEVERITY.ERROR -> Violation.Severity.ERROR
						},
						message = violation.message,
						specifics = violation.specifics.orEmpty(),
						location = Violation.Location(
							module = Violation.Module(
								path = result.subproject.path,
								name = result.subproject.name,
								projectDir = result.subproject.projectDir,
								rootDir = result.subproject.rootDir,
							),
							task = result.task,
							variant = result.gathererName,
							file = result.subproject.file(violation.file),
							startLine = violation.startLine,
							endLine = violation.endLine,
							column = violation.column
						),
						source = Violation.Source(
							gatherer = result.displayName,
							parser = violation.parser.name,
							reporter = violation.reporter,
							source = violation.source,
							report = result.parsableReportLocation,
							humanReport = result.humanReportLocation,
						)
					)
				}
			)
		}
		val nullSafeSum = nullSafeSum { v: Violations? -> v?.violations?.size }
		@Suppress("UNCHECKED_CAST")
		processViolations(Grouper.create(deduplicate(results), nullSafeSum) as Grouper.Start<Violations>)
	}

	internal data class Result(
		val subproject: Result.Project,
		val gatherer: TaskReportGatherer<Task>,
		val task: String,
		val displayName: String,
		val gathererName: String,
		val parsableReportLocation: File,
		val humanReportLocation: File,
	) : Serializable {
		internal data class Project(
			val name: String,
			val path: String,
			val rootDir: File,
			val projectDir: File,
		) : Serializable {

			@Suppress("DataClassContainsFunctions")
			internal fun file(path: String): File =
				projectDir.resolve(path)

			companion object {
				@Suppress("UnusedPrivateProperty") // Java magic.
				private const val serialVersionUID: Long = 1
			}
		}

		companion object {
			@Suppress("UnusedPrivateProperty") // Java magic.
			private const val serialVersionUID: Long = 1
		}
	}

	companion object {
		@Suppress("UNCHECKED_CAST")
		private val GATHERERS: List<TaskReportGatherer<Task>> = run {
			val gradleGatherers = listOf(
				QualityTaskReportGatherer(
					baseName = "checkstyle",
					displayName = "checkstyle",
					taskType = Checkstyle::class.java,
					parser = Parser.CHECKSTYLE,
				),
				QualityTaskReportGatherer(
					baseName = "pmd",
					displayName = "pmd",
					taskType = Pmd::class.java,
					parser = Parser.PMD,
				),
//				QualityTaskReportGatherer(
//					baseName = "cpd",
//					displayName = "cpd",
//					taskType = Cpd::class.java,
//					parser = Parser.CPD,
//					{it.reports.xml.destination},
//				),
//				TestReportGatherer<>("test", Test),
			)
			val agpGatherers = listOf(
				LintReportGatherer(),
			)
			return@run (gradleGatherers + agpGatherers) as List<TaskReportGatherer<Task>>
		}
	}
}
