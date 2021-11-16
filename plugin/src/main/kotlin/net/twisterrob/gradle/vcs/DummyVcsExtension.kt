package net.twisterrob.gradle.vcs

internal object DummyVcsExtension : VCSExtension {

	override val isAvailableQuick: Boolean = false
	override val isAvailable: Boolean = isAvailableQuick
	override val revision: String = "no VCS"
	override val revisionNumber: Int = 0
}
