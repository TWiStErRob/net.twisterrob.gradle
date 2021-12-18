package net.twisterrob.gradle.vcs

import org.gradle.api.Project
import org.gradle.api.file.FileCollection

interface VCSExtension {

	val isAvailable: Boolean
	val isAvailableQuick: Boolean
	val revision: String
	val revisionNumber: Int
	fun files(project: Project): FileCollection
}
