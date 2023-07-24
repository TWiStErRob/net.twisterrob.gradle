package net.twisterrob.gradle.internal.nagging

fun allowUnlimitedStacksForNagging() {
	GradleNaggingReflection.remainingStackTraces.set(Integer.MAX_VALUE)
}
