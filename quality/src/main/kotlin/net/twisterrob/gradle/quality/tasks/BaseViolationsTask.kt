package net.twisterrob.gradle.quality.tasks

import net.twisterrob.gradle.checkstyle.CheckStyleTask
import net.twisterrob.gradle.common.grouper.Grouper
import net.twisterrob.gradle.common.nullSafeSum
import net.twisterrob.gradle.pmd.PmdTask
import net.twisterrob.gradle.quality.Violation
import net.twisterrob.gradle.quality.Violations
import net.twisterrob.gradle.quality.gather.LintReportGatherer
import net.twisterrob.gradle.quality.gather.QualityTaskReportGatherer
import net.twisterrob.gradle.quality.gather.TaskReportGatherer
import net.twisterrob.gradle.quality.report.html.deduplicate
import net.twisterrob.gradle.quality.violations.RuleCategoryParser
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask
import se.bjurr.violations.lib.model.SEVERITY
import se.bjurr.violations.lib.reports.Parser

@UntrackedTask(because = "Abstract super-class, not to be instantiated directly")
abstract class BaseViolationsTask : DefaultTask() {

	init {
		this.group = JavaBasePlugin.VERIFICATION_GROUP
		// REPORT this extra task is needed because configureEach runs before
		// the tasks own configuration block from `tasks.create(..., configuration: Action)`.
		// `afterEvaluate` is not enough either because it breaks submodules the same way
		// same-module configuration breaks without `doFirst`.
		// `doFirst` doesn't work here, because reportTask may be UP-TO-DATE
		// `finalizedBy` doesn't work, because reportTask may be UP-TO-DATE
		// Last debugged in AGP 3.2.1 / Gradle 4.9
		val addInputTaskName = "${this.name}LateConfiguration"
		val addInputTask = this.project.tasks.register(addInputTaskName) { task ->
			task.doLast {
				forAllReportTasks { gatherer, reportTask ->
					// make sure external reports are involved in UP-TO-DATE checks
					val report = gatherer.getParsableReportLocation(reportTask)
					// Using files instead of file, because the report might not exist,
					// see https://github.com/gradle/gradle/issues/2919#issuecomment-981097984.
					this.inputs.files(report)
					if (!report.exists()) {
						logger.info(
							"Missing report for {} (probably wasn't executed yet after clean): {}",
							reportTask,
							report
						)
					}
				}
			}
		}
		this.dependsOn(addInputTask)
		forAllReportTasks { _, reportTask ->
			// make sure inputs are collected after the report task executed
			addInputTask.configure { it.mustRunAfter(reportTask) }
			// make sure inputs are available when running validation, but don't execute (depend on) reports
			this.mustRunAfter(reportTask)
		}
	}

	protected abstract fun processViolations(violations: Grouper.Start<Violations>)

	@TaskAction
	fun validateViolations() {
		val ruleCategoryParser = RuleCategoryParser()
		val results = project.allprojects.flatMap { subproject ->
			GATHERERS.flatMap { gatherer ->
				gatherer.allTasksFrom(subproject).map { task ->
					Violations(
						parser = gatherer.getDisplayName(task),
						module = subproject.path,
						variant = gatherer.getName(task),
						result = gatherer.getParsableReportLocation(task),
						report = gatherer.getHumanReportLocation(task),
						violations = gatherer.getViolations(task)?.map {
							Violation(
								rule = ruleCategoryParser.rule(it),
								category = ruleCategoryParser.category(it),
								severity = when (it.severity!!) {
									SEVERITY.INFO -> Violation.Severity.INFO
									SEVERITY.WARN -> Violation.Severity.WARNING
									SEVERITY.ERROR -> Violation.Severity.ERROR
								},
								message = it.message,
								specifics = it.specifics.orEmpty(),
								location = Violation.Location(
									module = subproject,
									task = task,
									variant = gatherer.getName(task),
									file = subproject.file(it.file),
									startLine = it.startLine,
									endLine = it.endLine,
									column = it.column
								),
								source = Violation.Source(
									gatherer = gatherer.getDisplayName(task),
									parser = it.parser.name,
									reporter = it.reporter,
									source = it.source,
									report = gatherer.getParsableReportLocation(task),
									humanReport = gatherer.getHumanReportLocation(task)
								)
							)
						}
					)
				}
			}
		}
		val nullSafeSum = nullSafeSum { v: Violations? -> v?.violations?.size }
		@Suppress("UNCHECKED_CAST")
		processViolations(Grouper.create(deduplicate(results), nullSafeSum) as Grouper.Start<Violations>)
	}

	private fun forAllReportTasks(action: (gatherer: TaskReportGatherer<Task>, reportTask: Task) -> Unit) {
		project.allprojects { subproject: Project ->
			GATHERERS.forEach { gatherer ->
				// TODO this should be configureEach or other lazy approach, but doesn't work on AGP 3.3 then
				gatherer.allTasksFrom(subproject).forEach { reportTask ->
					try {
						action(gatherer, reportTask)
					} catch (@Suppress("TooGenericExceptionCaught") ex: RuntimeException) {
						// Slap on more information to the exception.
						throw GradleException("Cannot configure $reportTask from $gatherer gatherer in $subproject", ex)
					}
				}
			}
		}
	}

	companion object {
		@Suppress("UNCHECKED_CAST")
		private val GATHERERS: List<TaskReportGatherer<Task>> = run {
			val gradleGatherers = listOf(
				QualityTaskReportGatherer("checkstyle", CheckStyleTask::class.java, Parser.CHECKSTYLE),
				QualityTaskReportGatherer("pmd", PmdTask::class.java, Parser.PMD),
//				ViolationChecker("cpd", Cpd::class.java, Parser.CPD, {it.reports.xml.destination}),
//				TestReportGatherer<>("test", Test),
			)
			val agpGatherers = listOf(
				LintReportGatherer(),
			)
			return@run (gradleGatherers + agpGatherers) as List<TaskReportGatherer<Task>>
		}
	}
}
