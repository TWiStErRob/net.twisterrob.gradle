package net.twisterrob.gradle.quality

import net.twisterrob.gradle.checkstyle.CheckStyleTask
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import se.bjurr.violations.lib.model.Violation
import se.bjurr.violations.lib.reports.Parser

class ValidateViolationsTask extends DefaultTask {

	Action<Map<String, Map<String, List<Violation>>>> action = Closure.IDENTITY as Action

	@SuppressWarnings("GroovyUnusedDeclaration")
	@TaskAction
	printViolationCounts() {
		Map<String, Map<String, List<Violation>>> results = project.subprojects.collectEntries {
			[ (it.path): it.tasks.withType(CheckStyleTask).collectEntries {
				def inputs = Collections.singletonList(it.reports.xml.destination)
				[ (it.variant.getAt('name')): Parser.CHECKSTYLE.findViolations(inputs) ]
			} ]
		}
		action.execute(results)
	}

	Action<Map<String, Map<String, List<Violation>>>> getAction() {
		return action
	}

	void setAction(Action<Map<String, Map<String, List<Violation>>>> action) {
		this.action = action
	}
}
