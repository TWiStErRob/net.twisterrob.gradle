package net.twisterrob.gradle.vcs

internal object DummyVcsExtension : VCSExtension {
	override val isAvailableQuick = false
	override val isAvailable = isAvailableQuick
	override val revision = "no VCS"
	override val revisionNumber = 0
}
