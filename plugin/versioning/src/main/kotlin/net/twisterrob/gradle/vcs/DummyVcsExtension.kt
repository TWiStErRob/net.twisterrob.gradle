package net.twisterrob.gradle.vcs

import org.gradle.api.Project
import org.gradle.api.file.FileCollection

internal object DummyVcsExtension : VCSExtension {

	override val isAvailableQuick: Boolean = false
	override val isAvailable: Boolean = isAvailableQuick
	override val revision: String = "no VCS"
	override val revisionNumber: Int = 0
	override fun files(project: Project): FileCollection = project.files()
}
