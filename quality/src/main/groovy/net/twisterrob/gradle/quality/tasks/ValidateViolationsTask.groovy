package net.twisterrob.gradle.quality.tasks

import com.android.build.gradle.tasks.LintGlobalTask
import com.android.build.gradle.tasks.LintPerVariantTask
import groovy.transform.CompileDynamic
import net.twisterrob.gradle.checkstyle.CheckStyleTask
import net.twisterrob.gradle.common.Utils
import net.twisterrob.gradle.common.grouper.Grouper
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
import org.gradle.api.tasks.TaskAction
import se.bjurr.violations.lib.model.Violation
import se.bjurr.violations.lib.reports.Parser

class ValidateViolationsTask extends DefaultTask {

	Action<Grouper.Start<Violations>> action = ValidateViolationsTask.&defaultAction as Action

	private static final TaskReportGatherer[] GATHERERS = [
			new QualityTaskReportGatherer(displayName: "checkstyle", taskType: CheckStyleTask,
					parser: Parser.CHECKSTYLE),
			new QualityTaskReportGatherer(displayName: "pmd", taskType: PmdTask, parser: Parser.PMD),
//		        new ViolationChecker("cpd", Parser.CPD, Cpd,
//				    {'TODO'}, {it.reports.xml.destination}))
//				new ViolationChecker("findbugs", FindBugs, ValidateViolationsTask.<FindBugs>
//						parser(Parser.FINDBUGS, {it.reports.xml.destination}),
//						{'TODO'}, {it.reports.xml.destination}),
			new LintReportGatherer<>(displayName: "lintVariant", taskType: LintPerVariantTask),
			new LintReportGatherer<>(displayName: "lint", taskType: LintGlobalTask),
//			new TestReportGatherer<>(displayName: "test", taskType: Test)
	]

	ValidateViolationsTask() {
		def thisTask = this
		project.rootProject.allprojects {Project subproject ->
			GATHERERS.each {TaskReportGatherer gatherer ->
				subproject.tasks.withType(gatherer.taskType).all {Task reportTask ->
					thisTask.mustRunAfter reportTask
				}
			}
		}
	}

	@SuppressWarnings("GroovyUnusedDeclaration")
	@CompileDynamic // Groovy static compiler can't handle this complexity, lol?
	@TaskAction
	validateViolations() {
		def results = project.subprojects.collectMany {Project subproject ->
			GATHERERS.collectMany {TaskReportGatherer gatherer ->
				subproject.tasks.withType(gatherer.taskType).collect {Task task ->
					new Violations(
							parser: gatherer.displayName,
							module: subproject.path,
							variant: gatherer.getName(task),
							report: gatherer.getReportLocation(task),
							violations: gatherer.getViolations(task)
					)
				}
			}
		}
		def countingReducer = Utils.nullSafeSum({Violations v -> v.violations?.size()})
		action.execute(Grouper.create(results, countingReducer))
	}

	@SuppressWarnings("GroovyUnusedDeclaration") // DSL
	Action<Grouper.Start<Violations>> getAction() {
		return action
	}

	@SuppressWarnings("GroovyUnusedDeclaration") // DSL
	void setAction(Action<Grouper.Start<Violations>> action) {
		this.action = action
	}

	@CompileDynamic
	static defaultAction(Grouper.Start<Violations> violations) {
		def grouped = violations.count().by.module.variant.parser.group()
		def table = new TableGenerator(
				zeroCount: '.' /*TODO ✓*/,
				missingCount: '',
				printEmptyRows: false,
				printEmptyColumns: false,
		).build(grouped as Map)
		def result = violations.list
		                       .collectMany {v -> (v.violations?: [ ]).collect {[ v, it ]}}
		                       .collect {pair ->
			Violations group; Violation violation; (group, violation) = pair
			def message = violation.message.replaceAll(/(\r?\n)+/, System.lineSeparator())
			"""\
${group.module}/${group.variant} ${violation.file}:${violation.startLine}
	${violation.reporter}/${violation.rule | "Unknown"}
${message.replaceAll(/(?m)^/, '\t')}\
"""
		} as List<String>
		def reportLocations = violations
				.list
				.grep {Violations v -> (v.violations?.size()?: 0) > 0}
				.collect {"${it.module}:${it.parser}@${it.variant} (${it.violations.size()}): ${it.report}"}

		println result.join(System.lineSeparator() + System.lineSeparator())
		println()
		println reportLocations.join(System.lineSeparator())
		println()
		println table
	}
}
