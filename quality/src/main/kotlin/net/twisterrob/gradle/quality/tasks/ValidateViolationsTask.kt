package net.twisterrob.gradle.quality.tasks

import net.twisterrob.gradle.checkstyle.CheckStyleTask
import net.twisterrob.gradle.common.AGPVersions
import net.twisterrob.gradle.common.grouper.Grouper
import net.twisterrob.gradle.common.nullSafeSum
import net.twisterrob.gradle.pmd.PmdTask
import net.twisterrob.gradle.quality.Violation
import net.twisterrob.gradle.quality.Violations
import net.twisterrob.gradle.quality.gather.LintGlobalReportGathererPre7
import net.twisterrob.gradle.quality.gather.LintReportGatherer
import net.twisterrob.gradle.quality.gather.LintVariantReportGathererPre7
import net.twisterrob.gradle.quality.gather.QualityTaskReportGatherer
import net.twisterrob.gradle.quality.gather.TaskReportGatherer
import net.twisterrob.gradle.quality.report.TableGenerator
import net.twisterrob.gradle.quality.report.html.deduplicate
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.TaskAction
import se.bjurr.violations.lib.model.SEVERITY
import se.bjurr.violations.lib.reports.Parser

open class ValidateViolationsTask : DefaultTask() {

	companion object {
		@Suppress("UNCHECKED_CAST")
		private val GATHERERS: List<TaskReportGatherer<Task>> = run {
			val gradleGatherers = listOf(
				QualityTaskReportGatherer("checkstyle", CheckStyleTask::class.java, Parser.CHECKSTYLE),
				QualityTaskReportGatherer("pmd", PmdTask::class.java, Parser.PMD),
//			ViolationChecker("cpd", Cpd::class.java, Parser.CPD, {it.reports.xml.destination})
//			TestReportGatherer<>("test", Test)
			)
			val agpGatherers = when {
				AGPVersions.CLASSPATH >= AGPVersions.v70x ->
					listOf(
						LintReportGatherer(),
					)
				else ->
					listOf(
						LintVariantReportGathererPre7(),
						LintGlobalReportGathererPre7(),
					)
			}
			return@run (gradleGatherers + agpGatherers) as List<TaskReportGatherer<Task>>
		}
	}

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

	@TaskAction
	fun validateViolations() {
		val results = project.allprojects.flatMap { subproject ->
			GATHERERS.flatMap { gatherer ->
				gatherer.allTasksFrom(subproject).map { task ->
					return@map Violations(
						parser = gatherer.displayName,
						module = subproject.path,
						variant = gatherer.getName(task),
						result = gatherer.getParsableReportLocation(task),
						report = gatherer.getHumanReportLocation(task),
						violations = gatherer.getViolations(task)?.map {
							Violation(
								rule = when (it.reporter) {
									Parser.CHECKSTYLE.name ->
										it.rule
											.substringAfterLast('.') // class name
											.removeSuffix("Check")
									else ->
										it.rule
								},
								category = when (it.reporter) {
									Parser.CHECKSTYLE.name ->
										it.rule
											.substringBeforeLast('.') // package
											.substringAfterLast('.') // last subpackage
											.capitalize()
									Parser.PMD.name ->
										when (it.category) {
											"Import Statements" -> "Imports"
											else -> it.category
										}
									else ->
										it.category
								},
								severity = when (it.severity!!) {
									SEVERITY.INFO -> Violation.Severity.INFO
									SEVERITY.WARN -> Violation.Severity.WARNING
									SEVERITY.ERROR -> Violation.Severity.ERROR
								},
								message = it.message,
								specifics = it.specifics ?: emptyMap(),
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
									gatherer = gatherer.displayName,
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
				// FIXME this should be configureEach or other lazy approach, but doesn't work on AGP 3.3 then
				gatherer.allTasksFrom(subproject).forEach { reportTask ->
					try {
						action(gatherer, reportTask)
					} catch (ex: RuntimeException) {
						throw GradleException(
							"Cannot configure $reportTask from $gatherer gatherer in $subproject", ex
						)
					}
				}
			}
		}
	}

	protected open fun processViolations(violations: Grouper.Start<Violations>) {
		@Suppress("UNCHECKED_CAST")
		val grouped = violations
			.count<Int>()
			.by("module")
			.by("variant")
			.by("parser")
			.group() as Map<String, Map<String, Map<String, Int?>>>
		val table = TableGenerator(
			zeroCount = "." /*TODO ✓*/,
			missingCount = "",
			printEmptyRows = false,
			printEmptyColumns = false
		).build(grouped)
		val result = violations
			.list
			.flatMap { it.violations ?: emptyList() }
			.map { violation ->
				val message = violation.message.replace("""(\r?\n)+""".toRegex(), System.lineSeparator())
				val loc = violation.location
				return@map (""
						+ "\n${loc.file.absolutePath}:${loc.startLine} in ${loc.module}/${loc.variant}"
						+ "\n\t${violation.source.reporter}/${violation.rule}"
						+ "\n${message.prependIndent("\t")}"
						)
			}
		val reportLocations = violations
			.list
			.filter { (it.violations ?: emptyList()).isNotEmpty() }
			.map { "${it.module}:${it.parser}@${it.variant} (${it.violations!!.size}): ${it.report}" }

		if (result.isNotEmpty()) {
			println(result.joinToString(System.lineSeparator() + System.lineSeparator()))
			println()
		}
		if (reportLocations.isNotEmpty()) {
			println(reportLocations.joinToString(System.lineSeparator()))
			println()
		}
		if (table.isNotBlank()) {
			println(table)
		}
	}
}
