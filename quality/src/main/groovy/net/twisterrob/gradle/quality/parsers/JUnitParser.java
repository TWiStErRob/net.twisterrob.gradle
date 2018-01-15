package net.twisterrob.gradle.quality.parsers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import se.bjurr.violations.lib.model.SEVERITY;
import se.bjurr.violations.lib.model.Violation;
import se.bjurr.violations.lib.parsers.ViolationsParser;
import se.bjurr.violations.lib.reports.Parser;

import static se.bjurr.violations.lib.model.Violation.violationBuilder;
import static se.bjurr.violations.lib.util.ViolationParserUtils.getAttribute;
import static se.bjurr.violations.lib.util.ViolationParserUtils.getChunks;
import static se.bjurr.violations.lib.util.ViolationParserUtils.getContent;

public class JUnitParser implements ViolationsParser {

	@Override
	public List<Violation> parseReportOutput(String reportContent) {
		List<Violation> violations = new ArrayList<>();
		List<String> tests = getChunks(reportContent, "<testcase", "</testcase>");
		for (String testChunk : tests) {
			String className = getAttribute(testChunk, "classname");
			String methodName = getAttribute(testChunk, "name");
			List<String> skips = getChunks(testChunk, "<skipped", "/>");
			for (String skippedChunk : skips) {
				String message = getAttribute(skippedChunk, "message");
				violations.add(
						violationBuilder()
								.setParser(Parser.PITEST)
								.setReporter("JUnit")
								.setFile(className)
								.setStartLine(0)
								.setRule(methodName)
								.setSeverity(SEVERITY.WARN)
								.setMessage(message)
								.build()
				);
			}
			List<String> fails = getChunks(testChunk, "<failure", "</failure>");
			for (String failureChunk : fails) {
				String message = getAttribute(failureChunk, "message");
				String stack = getContent(failureChunk, "failure");
				violations.add(
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
				);
			}
		}
		return violations;
	}
}
