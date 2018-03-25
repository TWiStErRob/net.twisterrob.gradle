package net.twisterrob.gradle.quality.tasks

import com.android.build.gradle.tasks.LintGlobalTask
import com.android.build.gradle.tasks.LintPerVariantTask
import net.twisterrob.gradle.checkstyle.CheckStyleTask
import net.twisterrob.gradle.common.grouper.Grouper
import net.twisterrob.gradle.common.nullSafeSum
import net.twisterrob.gradle.pmd.PmdTask
import net.twisterrob.gradle.quality.Violations
import net.twisterrob.gradle.quality.gather.LintReportGatherer
import net.twisterrob.gradle.quality.gather.QualityTaskReportGatherer
import net.twisterrob.gradle.quality.gather.TaskReportGatherer
import net.twisterrob.gradle.quality.report.TableGenerator
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import se.bjurr.violations.lib.reports.Parser

open class ValidateViolationsTask : DefaultTask() {

	@Input
	@Suppress("MemberVisibilityCanBePrivate") // DSL
	var action: Action<Grouper.Start<Violations>> = Action(::defaultAction)

	companion object {
		@Suppress("UNCHECKED_CAST")
		val GATHERERS = listOf(
				QualityTaskReportGatherer("checkstyle", CheckStyleTask::class.java, Parser.CHECKSTYLE),
				QualityTaskReportGatherer("pmd", PmdTask::class.java, Parser.PMD),
//		        ViolationChecker("cpd", Cpd::class.java, Parser.CPD,
//				    {'TODO'}, {it.reports.xml.destination}))
//				ViolationChecker("findbugs", FindBugs, ValidateViolationsTask.<FindBugs>
//						parser(Parser.FINDBUGS, {it.reports.xml.destination}),
//						{'TODO'}, {it.reports.xml.destination}),
				LintReportGatherer("lintVariant", LintPerVariantTask::class.java),
				LintReportGatherer("lint", LintGlobalTask::class.java)
//			TestReportGatherer<>("test", Test)
		) as List<TaskReportGatherer<Task>>
	}

	init {
		project.rootProject.allprojects { subproject: Project ->
			GATHERERS.forEach { gatherer ->
				subproject.tasks.withType(gatherer.taskType).all { reportTask ->
					this@ValidateViolationsTask.mustRunAfter(reportTask)
				}
			}
		}
	}

	@Suppress("unused")
	@TaskAction
	fun validateViolations() {
		val results = project.subprojects.flatMap { subproject ->
			GATHERERS.flatMap { gatherer ->
				subproject.tasks.withType(gatherer.taskType).map { task ->
					Violations(
							parser = gatherer.displayName,
							module = subproject.path,
							variant = gatherer.getName(task),
							result = gatherer.getParsableReportLocation(task),
							report = gatherer.getHumanReportLocation(task),
							violations = gatherer.getViolations(task)
					)
				}
			}
		}
		val nullSafeSum = nullSafeSum(java.util.function.Function({ v: Violations? -> v?.violations?.size }))
		@Suppress("UNCHECKED_CAST")
		action.execute(Grouper.create(results, nullSafeSum) as Grouper.Start<Violations>)
	}
}

private fun defaultAction(violations: Grouper.Start<Violations>) {
	val grouped = violations.count<Int>().by("module").by("variant").by("parser").group()
	@Suppress("UNCHECKED_CAST")
	val table = TableGenerator(
			zeroCount = "." /*TODO ✓*/,
			missingCount = "",
			printEmptyRows = false,
			printEmptyColumns = false
	).build(grouped as Map<String, Map<String, Map<String, Int?>>>)
	val result: List<String> = violations.list
			.flatMap { v -> (v.violations ?: listOf()).map { Pair(v, it) } }
			.map { (group, violation) ->
				val message = violation.message.replace("""(\r?\n)+""".toRegex(), System.lineSeparator())
				return@map """
					${group.module}/${group.variant} ${violation.file}:${violation.startLine}
						${violation.reporter}/${violation.rule.or("Unknown")}
${message.replace("""(?m)^""".toRegex(), "\t\t\t\t\t\t")}
				""".trimIndent()
			}
	val reportLocations = violations
			.list
			.filter { v -> (v.violations?.size ?: 0) > 0 }
			.map { "${it.module}:${it.parser}@${it.variant} (${it.violations!!.size}): ${it.report}" }

	println(result.joinToString(System.lineSeparator() + System.lineSeparator()))
	println()
	println(reportLocations.joinToString(System.lineSeparator()))
	println()
	println(table)
}
