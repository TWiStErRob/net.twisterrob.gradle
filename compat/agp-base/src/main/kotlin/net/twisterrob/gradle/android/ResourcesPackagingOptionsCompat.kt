package net.twisterrob.gradle.android

interface ResourcesPackagingOptionsCompat {
	val excludes: MutableSet<String>
	val pickFirsts: MutableSet<String>
	val merges: MutableSet<String>
}
