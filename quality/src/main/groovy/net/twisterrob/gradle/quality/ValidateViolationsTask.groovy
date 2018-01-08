package net.twisterrob.gradle.quality

import net.twisterrob.gradle.checkstyle.CheckStyleTask
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction
import se.bjurr.violations.lib.model.Violation
import se.bjurr.violations.lib.reports.Parser

class ValidateViolationsTask extends DefaultTask {

	@SuppressWarnings("GroovyUnusedDeclaration")
	@TaskAction
	printViolationCounts() {
		def violations = project
				.subprojects
				.collectMany {it.tasks.withType(CheckStyleTask)}
				.collect(
				{CheckStyleTask task ->
					new Check(
							task: task,
							report: task.reports.xml.destination,
							parser: Parser.CHECKSTYLE
					)
				})
				.each(
				{Check check ->
					def inputs = Collections.singletonList(check.report)
					check.violations = check.parser.findViolations(inputs)
				})

		println "Violations: ${violations.sum {Check check -> check.violations.size()}}"
	}

	static class Check {

		Task task
		File report
		Parser parser
		List<Violation> violations
	}
}
