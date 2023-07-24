package net.twisterrob.gradle.internal.nagging

import org.gradle.api.invocation.Gradle
import org.gradle.util.GradleVersion
import org.slf4j.LoggerFactory

internal fun Gradle.allowUnlimitedStacksForNagging() {
	if (GradleVersion.version("8.3") <= GradleVersion.current().baseVersion) {
		try {
			GradleNaggingReflection.remainingStackTraces.set(Integer.MAX_VALUE)
		} catch(ex: ReflectiveOperationException) {
			val logger = LoggerFactory.getLogger("allowUnlimitedStacksForNagging")
			logger.warn("Failed to set unlimited stack traces for nagging.", ex)
		}
	}
}
