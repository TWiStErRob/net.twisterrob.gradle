package net.twisterrob.gradle.quality.tasks

import com.android.build.gradle.tasks.LintGlobalTask
import com.android.build.gradle.tasks.LintPerVariantTask
import groovy.transform.CompileDynamic
import net.twisterrob.gradle.checkstyle.CheckStyleTask
import net.twisterrob.gradle.common.grouper.Grouper
import net.twisterrob.gradle.pmd.PmdTask
import net.twisterrob.gradle.quality.Violations
import net.twisterrob.gradle.quality.gather.LintReportGatherer
import net.twisterrob.gradle.quality.gather.QualityTaskReportGatherer
import net.twisterrob.gradle.quality.gather.TaskReportGatherer
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction
import se.bjurr.violations.lib.reports.Parser

import java.util.stream.Collectors

class ValidateViolationsTask extends DefaultTask {

	Action<Grouper.Start<Violations>> action = Closure.IDENTITY as Action

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
		def countingReducer = Collectors.
				reducing(null, {Violations it -> it.violations?.size()}, ValidateViolationsTask.&safeAdd)
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
	private static <T> T safeAdd(T a, T b) {
		if (a != null && b != null) {
			return a + b
		} else if (a != null && b == null) {
			return a
		} else if (a == null && b != null) {
			return b
		} else /* (a == null && b == null) */ {
			return null
		}
	}
}
