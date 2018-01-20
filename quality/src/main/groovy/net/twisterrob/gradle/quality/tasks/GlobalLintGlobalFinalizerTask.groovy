package net.twisterrob.gradle.quality.tasks

import com.android.build.gradle.tasks.LintGlobalTask
import net.twisterrob.gradle.common.AndroidVariantApplier
import net.twisterrob.gradle.common.Utils
import net.twisterrob.gradle.quality.gather.LintReportGatherer
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import se.bjurr.violations.lib.model.Violation

class GlobalLintGlobalFinalizerTask extends DefaultTask {

	private List<File> xmlReports = [ ]

	GlobalLintGlobalFinalizerTask() {
		project.allprojects.each {Project subproject ->
			new AndroidVariantApplier(subproject).applyAfterPluginConfigured {
				mustRunAfter subproject.tasks.withType(LintGlobalTask) {LintGlobalTask subTask ->
					subTask.lintOptions.abortOnError = Utils.wasExplicitlyLaunched(subTask)
					// make sure we have xml output, otherwise can't figure out if it failed
					subTask.lintOptions.xmlReport = true
					xmlReports.add(Utils.getXmlOutput(subTask))
				}
			}
		}
	}

	@SuppressWarnings("GroovyUnusedDeclaration")
	@TaskAction
	def failOnFailures() {
		def gatherer = new LintReportGatherer()
		Map<File, List<Violation>> violationsByFile = xmlReports.collectEntries {[ (it): gatherer.findViolations(it) ]}
		def totalCount = violationsByFile.values().sum {List<Violation> violations -> violations.size()} as int
		if (totalCount > 0) {
			def reportsWithCounts = violationsByFile.collect({report, violations -> "${report} (${violations.size()})"})
			throw new GradleException(
					"Ran lint on subprojects: ${totalCount} issues found${System.lineSeparator()}" +
							"See reports in subprojects:${System.lineSeparator()}" +
							"${reportsWithCounts.join(System.lineSeparator())}")
		}
	}
}
