package net.twisterrob.gradle.quality.parsers

import se.bjurr.violations.lib.model.SEVERITY
import se.bjurr.violations.lib.model.Violation
import se.bjurr.violations.lib.model.Violation.violationBuilder
import se.bjurr.violations.lib.parsers.ViolationsParser
import se.bjurr.violations.lib.reports.Parser
import se.bjurr.violations.lib.util.ViolationParserUtils.getAttribute
import se.bjurr.violations.lib.util.ViolationParserUtils.getChunks
import se.bjurr.violations.lib.util.ViolationParserUtils.getContent
import java.util.*

class JUnitParser : ViolationsParser {

	override fun parseReportOutput(reportContent: String): List<Violation> {
		val violations = mutableListOf<Violation>()
		val tests: List<String> = getChunks(reportContent, "<testcase", "</testcase>")
		for (testChunk in tests) {
			val className = getAttribute(testChunk, "classname")!!
			val methodName = getAttribute(testChunk, "name")!!
			violations += parseSkips(testChunk, className, methodName)
			violations += parseFailures(testChunk, className, methodName)
		}
		return violations
	}

	private fun parseSkips(testChunk: String, className: String, methodName: String): List<Violation> =
			getChunks(testChunk, "<skipped", "/>").map { skippedChunk ->
				val message = getAttribute(skippedChunk, "message")
				violationBuilder()
						.setParser(Parser.PITEST)
						.setReporter("JUnit")
						.setFile(className)
						.setStartLine(0)
						.setRule(methodName)
						.setSeverity(SEVERITY.WARN)
						.setMessage(message)
						.build()
			}

	private fun parseFailures(testChunk: String, className: String, methodName: String): List<Violation> =
			getChunks(testChunk, "<failure", "</failure>").map { failureChunk ->
				val message = getAttribute(failureChunk, "message")
				val stack = getContent(failureChunk, "failure")
				violationBuilder()
						.setParser(Parser.PITEST)
						.setReporter("JUnit")
						.setFile(className)
						.setStartLine(0)
						.setRule(methodName)
						.setSeverity(SEVERITY.ERROR)
						.setMessage(message)
						.setSpecifics(Collections.singletonMap("stacktrace", stack))
						.build()
			}
}
