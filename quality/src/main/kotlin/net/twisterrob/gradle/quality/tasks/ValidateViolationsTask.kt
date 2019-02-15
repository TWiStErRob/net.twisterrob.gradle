package net.twisterrob.gradle.quality.tasks

import com.android.build.gradle.tasks.LintGlobalTask
import com.android.build.gradle.tasks.LintPerVariantTask
import net.twisterrob.gradle.checkstyle.CheckStyleTask
import net.twisterrob.gradle.common.grouper.Grouper
import net.twisterrob.gradle.common.nullSafeSum
import net.twisterrob.gradle.pmd.PmdTask
import net.twisterrob.gradle.quality.Violation
import net.twisterrob.gradle.quality.Violations
import net.twisterrob.gradle.quality.gather.LintReportGatherer
import net.twisterrob.gradle.quality.gather.QualityTaskReportGatherer
import net.twisterrob.gradle.quality.gather.TaskReportGatherer
import net.twisterrob.gradle.quality.report.TableGenerator
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.TaskAction
import se.bjurr.violations.lib.model.SEVERITY
import se.bjurr.violations.lib.reports.Parser

open class ValidateViolationsTask : DefaultTask() {

	@Suppress("MemberVisibilityCanBePrivate") // DSL
	var action: Action<Grouper.Start<Violations>> = Action(::defaultAction)

	companion object {
		@Suppress("UNCHECKED_CAST")
		private val GATHERERS = listOf(
			QualityTaskReportGatherer("checkstyle", CheckStyleTask::class.java, Parser.CHECKSTYLE),
			QualityTaskReportGatherer("pmd", PmdTask::class.java, Parser.PMD),
//			ViolationChecker("cpd", Cpd::class.java, Parser.CPD, {it.reports.xml.destination})
//			ViolationChecker("findbugs", FindBugs::class.java, Parser.FINDBUGS, {it.reports.xml.destination}),
			LintReportGatherer("lintVariant", LintPerVariantTask::class.java),
			LintReportGatherer("lint", LintGlobalTask::class.java)
//			TestReportGatherer<>("test", Test)
		) as List<TaskReportGatherer<Task>>
	}

	init {
		group = JavaBasePlugin.VERIFICATION_GROUP
		project.allprojects { subproject: Project ->
			// REPORT this is needed otherwise configureEach runs before the tasks own tasks.create(..., *configuration*)
			// Last debugged in AGP 3.2.1 / Gradle 4.9
			subproject.afterEvaluate {
				GATHERERS.forEach { gatherer ->
					subproject.tasks.withType(gatherer.taskType).configureEach { reportTask ->
						try {
							// make sure external reports are involved in UP-TO-DATE checks
							this@ValidateViolationsTask.inputs.file(gatherer.getParsableReportLocation(reportTask))
							// make sure inputs are available if many tasks are executed
							this@ValidateViolationsTask.mustRunAfter(reportTask)
						} catch (ex: RuntimeException) {
							throw GradleException(
								"Cannot configure $reportTask from $gatherer gatherer in $subproject", ex
							)
						}
					}
				}
			}
		}
	}

	@TaskAction
	fun validateViolations() {
		val results = project.allprojects.flatMap { subproject ->
			GATHERERS.flatMap { gatherer ->
				subproject.tasks.withType(gatherer.taskType).map { task ->
					Violations(
						parser = gatherer.displayName,
						module = subproject.path,
						variant = gatherer.getName(task),
						result = gatherer.getParsableReportLocation(task),
						report = gatherer.getHumanReportLocation(task),
						violations = gatherer.getViolations(task)?.map {
							Violation(
								rule = it.rule,
								category = it.category,
								severity = when (it.severity!!) {
									SEVERITY.INFO -> Violation.Severity.INFO
									SEVERITY.WARN -> Violation.Severity.WARNING
									SEVERITY.ERROR -> Violation.Severity.ERROR
								},
								message = it.message,
								specifics = it.specifics ?: emptyMap(),
								location = Violation.Location(
									module = subproject,
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
		val nullSafeSum = nullSafeSum(java.util.function.Function { v: Violations? -> v?.violations?.size })
		@Suppress("UNCHECKED_CAST")
		action.execute(Grouper.create(results, nullSafeSum) as Grouper.Start<Violations>)
	}
}

private fun defaultAction(violations: Grouper.Start<Violations>) {
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

	println(result.joinToString(System.lineSeparator() + System.lineSeparator()))
	println()
	println(reportLocations.joinToString(System.lineSeparator()))
	println()
	println(table)
}
