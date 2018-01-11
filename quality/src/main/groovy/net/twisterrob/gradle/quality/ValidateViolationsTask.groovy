package net.twisterrob.gradle.quality

import com.android.build.gradle.tasks.LintGlobalTask
import com.android.build.gradle.tasks.LintPerVariantTask
import net.twisterrob.gradle.checkstyle.CheckStyleTask
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.plugins.quality.FindBugs
import org.gradle.api.plugins.quality.Pmd
import org.gradle.api.tasks.TaskAction
import se.bjurr.violations.lib.model.Violation
import se.bjurr.violations.lib.reports.Parser

import java.util.function.Function

class ValidateViolationsTask extends DefaultTask {

	Action<Map<String, Map<String, Map<String, List<Violation>>>>> action = Closure.IDENTITY as Action

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
		Map<String, Map<String, Map<String, List<Violation>>>> results = new TreeMap<>()
		results.put("checkstyle", gatherResults(Parser.CHECKSTYLE, CheckStyleTask,
				{it.checkTargetName}, {it.reports.xml.destination}))
		results.put("pmd", gatherResults(Parser.PMD, Pmd,
				{'TODO'}, {it.reports.xml.destination}))
//		results.put("cpd", gatherResults(Parser.CPD, Cpd,
//				{'TODO'}, {it.reports.xml.destination}))
		results.put("findbugs", gatherResults(Parser.FINDBUGS, FindBugs,
				{'TODO'}, {it.reports.xml.destination}))
		results.put("lintVariant", gatherResults(Parser.ANDROIDLINT, LintPerVariantTask,
				{it.variantName}, {new File(it.reportsDir, "lint-results-${it.variantName}.xml")}))
		results.put("lint", gatherResults(Parser.ANDROIDLINT, LintGlobalTask,
				{it.name}, {new File(it.reportsDir, "lint-results.xml")}))
		action.execute(results)
	}

	private <T extends Task> Map<String, Map<String, List<Violation>>> gatherResults(
			Parser parser, Class<T> taskType, Function<T, String> namer, Function<T, File> reportGetter) {
		Map<String, Map<String, List<Violation>>> results = project.subprojects.collectEntries {
			[ (it.path): it.tasks.withType(taskType).collectEntries {
				File report = reportGetter.apply(it)
				if (report.exists()) {
					List<File> inputs = Collections.singletonList(report)
					return [ (namer.apply(it)): parser.findViolations(inputs) ]
				} else {
					//logger.warn "${parser} report: '${report}' does not exist"
					return Collections.emptyMap()
				}
			} ]
		}
		return results
	}

	Action<Map<String, Map<String, Map<String, List<Violation>>>>> getAction() {
		return action
	}

	void setAction(Action<Map<String, Map<String, Map<String, List<Violation>>>>> action) {
		this.action = action
	}
}
