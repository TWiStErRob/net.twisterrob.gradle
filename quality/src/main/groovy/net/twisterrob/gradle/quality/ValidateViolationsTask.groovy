package net.twisterrob.gradle.quality

import com.android.build.gradle.tasks.LintGlobalTask
import com.android.build.gradle.tasks.LintPerVariantTask
import net.twisterrob.gradle.checkstyle.CheckStyleTask
import net.twisterrob.gradle.common.grouper.GrouperByer
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.quality.FindBugs
import org.gradle.api.plugins.quality.Pmd
import org.gradle.api.tasks.TaskAction
import se.bjurr.violations.lib.model.Violation
import se.bjurr.violations.lib.reports.Parser

import javax.annotation.Nonnull
import javax.annotation.Nullable
import java.util.function.Function

class ValidateViolationsTask extends DefaultTask {

	Action<GrouperByer.Chain<Violations>> action = Closure.IDENTITY as Action

	ValidateViolationsTask() {
		def that = this
		project.afterEvaluate {
			project.tasks.withType(CheckStyleTask) {that.mustRunAfter it}
			project.tasks.withType(Pmd) {that.mustRunAfter it}
			project.tasks.withType(FindBugs) {that.mustRunAfter it}
			project.tasks.withType(LintPerVariantTask) {that.mustRunAfter it}
			project.tasks.withType(LintGlobalTask) {that.mustRunAfter it}
		}
	}

	@SuppressWarnings("GroovyUnusedDeclaration")
	@TaskAction
	validateViolations() {
		List<Violations> results = new ArrayList<>()
		results.addAll(gatherResults("checkstyle", Parser.CHECKSTYLE, CheckStyleTask,
				{it.checkTargetName}, {it.reports.xml.destination}))
		results.addAll(gatherResults("pmd", Parser.PMD, Pmd,
				{'TODO'}, {it.reports.xml.destination}))
//		results.addAll(gatherResults("cpd", Parser.CPD, Cpd,
//				{'TODO'}, {it.reports.xml.destination}))
		results.addAll(gatherResults("findbugs", Parser.FINDBUGS, FindBugs,
				{'TODO'}, {it.reports.xml.destination}))
		results.addAll(gatherResults("lintVariant", Parser.ANDROIDLINT, LintPerVariantTask,
				{it.variantName}, {new File(it.reportsDir, "lint-results-${it.variantName}.xml")}))
		results.addAll(gatherResults("lint", Parser.ANDROIDLINT, LintGlobalTask,
				{it.name}, {new File(it.reportsDir, "lint-results.xml")}))
		action.execute(GrouperByer.group(results))
	}

	private <T extends Task> List<Violations> gatherResults(
			String displayName, Parser parser, Class<T> taskType, Function<T, String> namer,
			Function<T, File> reportGetter) {
		project.subprojects.collectMany {Project project ->
			project.tasks.withType(taskType).collect {T task ->
				File report = reportGetter.apply(task)
				List<Violation> violations
				if (report.exists()) {
					List<File> input = Collections.singletonList(report)
					violations = parser.findViolations(input)
				} else {
					//logger.warn "${parser} report: '${report}' does not exist"
					violations = null
				}
				return new Violations(
						parser: displayName,
						module: project.path,
						variant: namer.apply(task),
						report: report,
						violations: violations
				)
			}
		}
	}

	Action<GrouperByer.Chain<Violations>> getAction() {
		return action
	}

	void setAction(Action<GrouperByer.Chain<Violations>> action) {
		this.action = action
	}
}

class Violations {

	@Nonnull String parser
	@Nonnull String module
	@Nonnull String variant
	@Nonnull File report
	/**
	 * Report file missing, or error during read.
	 */
	@Nullable List<Violation> violations

	@Override
	String toString() {
		"${module}:${parser}@${variant} (${report}): ${violations}"
	}
}
