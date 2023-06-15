package net.twisterrob.gradle.logging

internal class JavaPackageHierarchyBasedMap<T>(
	private val redirects: Map<String, T> = emptyMap()
) {
	fun pickFor(name: String): T? {
		val names = generateSequence(name) {
			it.substringBeforeLast('.', "").takeIf(String::isNotEmpty)
		}
		val match = names.firstOrNull { redirects.containsKey(it) }
		return redirects[match]
	}
}
