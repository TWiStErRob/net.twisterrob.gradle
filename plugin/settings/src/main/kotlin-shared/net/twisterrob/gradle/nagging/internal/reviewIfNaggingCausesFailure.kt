package net.twisterrob.gradle.nagging.internal

import net.twisterrob.gradle.buildFinishedCompat
import net.twisterrob.gradle.isDoNotNagAboutDiagnosticsEnabled
import org.gradle.api.invocation.Gradle
import org.gradle.util.GradleVersion

/**
 * @see IgnoringSetGradle83
 */
internal fun Gradle.reviewIfNaggingCausesFailure() {
	if (GradleVersion.version("8.3") <= GradleVersion.current().baseVersion) {
		// Alternatives considered:
		// * `gradle.projectsEvaluated { clearErrorIfNoMessages() }`
		//   Too early in build lifecycle (e.g. task execution).
		// * `gradle.taskGraph.whenReady { clearErrorIfNoMessages() }`
		//   Too early in build lifecycle (e.g. task execution).
		// * `gradle.buildFinished { clearErrorIfNoMessages() }`
		//   It's around the right time, but it's deprecated.
		// * `FlowScope` is the only way I see now.
		//   It's available since Gradle 8.1, it's still incubating in 8.3.
		this.buildFinishedCompat { clearErrorIfNoMessages() }
	}
}

private fun clearErrorIfNoMessages() {
	if (GradleNaggingReflection.messages.isEmpty()) {
		if (isDoNotNagAboutDiagnosticsEnabled) {
			@Suppress("detekt.ForbiddenMethodCall") // This will be shown in the console, as the user explicitly asked for it.
			println(
				"No deprecation messages at the end of build, " +
						"resetting error from ${GradleNaggingReflection.error ?: "null"}."
			)
		}
		GradleNaggingReflection.error = null
	} else {
		if (isDoNotNagAboutDiagnosticsEnabled) {
			@Suppress("detekt.ForbiddenMethodCall") // This will be shown in the console, as the user explicitly asked for it.
			print(
				buildString {
					appendLine("Deprecation messages found, keeping error:")
					appendLine(GradleNaggingReflection.error)
					appendLine("Messages:")
					appendLine(GradleNaggingReflection.messages.joinToString("\n"))
				}
			)
		}
	}
}
