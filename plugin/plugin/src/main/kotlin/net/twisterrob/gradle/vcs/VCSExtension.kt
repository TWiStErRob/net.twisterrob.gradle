package net.twisterrob.gradle.vcs

interface VCSExtension {

	val isAvailable: Boolean
	val isAvailableQuick: Boolean
	val revision: String
	val revisionNumber: Int
}
