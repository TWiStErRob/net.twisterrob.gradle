package net.twisterrob.gradle.build.testing

import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestListener
import org.gradle.api.tasks.testing.TestOutputEvent
import org.gradle.api.tasks.testing.TestResult
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import java.util.EnumSet
import kotlin.math.absoluteValue

@Suppress(
	"detekt.CyclomaticComplexMethod", "detekt.CognitiveComplexMethod", // TODO
	"detekt.FunctionMaxLength" // Rather be explicit about what it does.
)
fun Test.configureVerboseReportsForGithubActions() {
	testLogging {
		// disable all events, output handled by custom callbacks below
		events = EnumSet.noneOf(TestLogEvent::class.java)
		//events = TestLogEvent.values().toSet() - TestLogEvent.STARTED
		exceptionFormat = TestExceptionFormat.FULL
		showExceptions = true
		showCauses = true
		showStackTraces = true
	}
	@Suppress("detekt.UseDataClass") // Wouldn't be used.
	class TestInfo(
		val descriptor: TestDescriptor,
		val stdOut: StringBuilder = StringBuilder(),
		val stdErr: StringBuilder = StringBuilder()
	)

	val lookup = mutableMapOf<TestDescriptor, TestInfo>()
	addTestOutputListener { descriptor: TestDescriptor, event: TestOutputEvent ->
		val info = lookup.getValue(descriptor)
		when (event.destination!!) {
			TestOutputEvent.Destination.StdOut -> info.stdOut.append(event.message)
			TestOutputEvent.Destination.StdErr -> info.stdErr.append(event.message)
		}
	}

	fun logResults(testType: String, descriptor: TestDescriptor, result: TestResult) {
		@Suppress("detekt.ForbiddenMethodCall") // Need to output raw as the result is parsed by GitHub Actions.
		val outputToConsole: (String) -> Unit = ::println

		fun fold(outputType: String, condition: Boolean, output: () -> Unit) {
			val id = descriptor.toString().hashCode().absoluteValue
			if (condition) {
				outputToConsole("::group::${testType}_${outputType}_${id}")
				output()
				outputToConsole("::endgroup:: ")
			}
		}

		val info = lookup.remove(descriptor) ?: error("Could not find ${descriptor} in ${lookup.keys}")
		val hasStdOut = info.stdOut.isNotEmpty()
		val hasStdErr = info.stdErr.isNotEmpty()
		val hasError = result.exception != null
		val hasAnything = hasStdOut || hasStdErr || hasError

		val groupSuite = "Suite"
		val groupClass = "Class"
		val groupName = when (val className = descriptor.className) {
			null -> groupSuite
			descriptor.name -> groupClass
			else -> className
		}
		val name = descriptor.name
		val fullName = "${groupName} > ${name}"
		if (groupName == groupSuite && name.startsWith("Gradle Test Executor") && !hasAnything) {
			// Don't log, this is because of concurrency.
			return
		} else if (groupName == groupSuite && name.startsWith("Gradle Test Run") && !hasAnything) {
			// Don't log, this is because of Gradle's system.
			return
		} else if (groupName == groupClass && !hasAnything) {
			// Don't log, individual tests are enough.
			return
		}

		outputToConsole("${fullName} ${result.resultType}")

		fold("ex", hasError) {
			outputToConsole("EXCEPTION ${fullName}")
			val ex = result.exception ?: error("Logic issue, hasError, but no exception")
			outputToConsole(ex.stackTraceToString())
		}
		fold("out", hasStdOut) {
			outputToConsole("STANDARD_OUT ${fullName}")
			outputToConsole(info.stdOut.toString())
		}
		fold("err", hasStdErr) {
			outputToConsole("STANDARD_ERR ${fullName}")
			outputToConsole(info.stdErr.toString())
		}
	}
	addTestListener(object : TestListener {
		override fun beforeSuite(suite: TestDescriptor) {
			lookup[suite] = TestInfo(suite)
		}

		override fun beforeTest(testDescriptor: TestDescriptor) {
			lookup[testDescriptor] = TestInfo(testDescriptor)
		}

		override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {
			logResults("test", testDescriptor, result)
		}

		override fun afterSuite(suite: TestDescriptor, result: TestResult) {
			logResults("suite", suite, result)
		}
	})
}
