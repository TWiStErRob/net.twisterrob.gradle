package net.twisterrob.gradle.internal.nagging

import net.twisterrob.gradle.isDoNotNagAboutDiagnosticsEnabled

internal interface IgnoringSet : MutableSet<String> {

	fun ignorePattern(regex: Regex)

	companion object {
		internal fun wrap(backingSet: MutableSet<String>): IgnoringSet =
			when {
				backingSet is IgnoringSet -> backingSet
				else -> IgnoringSetImpl(backingSet)
			}
	}
}

/**
 * Return value of [add] will control whether the deprecation is shown or not,
 * and whether the build will fail at the end.
 */
private class IgnoringSetImpl(
	private val backingSet: MutableSet<String>
) : IgnoringSet, MutableSet<String> by backingSet {

	private val ignorePatterns: MutableSet<Regex> = mutableSetOf()

	override fun ignorePattern(regex: Regex) {
		if (isDoNotNagAboutDiagnosticsEnabled) {
			@Suppress("ForbiddenMethodCall") // This will be shown in the console, as the user explicitly asked for it.
			println("Ignoring pattern: ${regex}")
		}
		ignorePatterns.add(regex)
	}

	override fun add(element: String): Boolean {
		val isIgnored = ignorePatterns.any { it.matches(element) }
		val isNew = backingSet.add(element)
		if (isDoNotNagAboutDiagnosticsEnabled) {
			@Suppress("ForbiddenMethodCall") // This will be shown in the console, as the user explicitly asked for it.
			println(buildDiagnostics(ignorePatterns, element, isNew))
		}
		return !isIgnored && isNew
	}
}

private fun buildDiagnostics(ignorePatterns: Set<Regex>, element: String, isNew: Boolean): String {
	val state = if (isNew) "first seen" else "already added"
	val ignores = ignorePatterns.joinToString(separator = "\n") { ignorePattern ->
		val matching = if (ignorePattern.matches(element)) "**matching**" else "not matching"
		val ignoreRegex = ignorePattern.toString().prependIndent("   ")
		@Suppress("StringShouldBeRawString") // It would be more complex because of interpolations.
		" - Deprecation is ${matching} ignore pattern:\n   ```regex\n${ignoreRegex}\n   ```"
	}
	@Suppress("StringShouldBeRawString") // It would be more complex because of interpolations.
	return "Nagging about ${state} deprecation:\n```\n${element}\n```\n${ignores}"
}
