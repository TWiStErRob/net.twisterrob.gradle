package net.twisterrob.gradle.internal.nagging

import net.twisterrob.gradle.isDoNotNagAboutDiagnosticsEnabled
import org.gradle.api.invocation.Gradle
import org.gradle.util.GradleVersion

/**
 * @see IgnoringSetGradle83
 */
internal fun reviewIfNaggingCausesFailure(gradle: Gradle) {
	if (GradleVersion.version("8.3") <= GradleVersion.current().baseVersion) {
		// Alternatives considered:
		// * `gradle.projectsEvaluated { clearErrorIfNoMessages() }`
		//   Too early in build lifecycle (e.g. task execution).
		// * `gradle.taskGraph.whenReady { clearErrorIfNoMessages() }`
		//   Too early in build lifecycle (e.g. task execution).
		@Suppress("DEPRECATION")
		gradle.buildFinished { clearErrorIfNoMessages() }
	}
}

private fun clearErrorIfNoMessages() {
	if (GradleNaggingReflection.messages.isEmpty()) {
		if (isDoNotNagAboutDiagnosticsEnabled) {
			println(
				"No deprecation messages at the end of build, " +
						"resetting error from ${GradleNaggingReflection.error}."
			)
		}
		GradleNaggingReflection.error = null
	} else {
		if (isDoNotNagAboutDiagnosticsEnabled) {
			println(
				"Deprecation messages found, keeping error:\n"
						+ "${GradleNaggingReflection.error}\n"
						+ "Messages:\n"
						+ GradleNaggingReflection.messages.joinToString("\n")
			)
		}
	}
}
