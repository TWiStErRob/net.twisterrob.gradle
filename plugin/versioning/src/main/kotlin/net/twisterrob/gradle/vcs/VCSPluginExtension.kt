package net.twisterrob.gradle.vcs

import net.twisterrob.gradle.kotlin.dsl.extensions
import net.twisterrob.gradle.vcs.git.GITPluginExtension
import net.twisterrob.gradle.vcs.svn.SVNPluginExtension
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.kotlin.dsl.getByName

@Suppress("detekt.UnnecessaryAbstractClass") // Gradle convention.
abstract class VCSPluginExtension : VCSExtension {

	var current: VCSExtension = DummyVcsExtension
		internal set

	val svn: SVNPluginExtension
		get() = extensions.getByName<SVNPluginExtension>(SVNPluginExtension.NAME)
	val git: GITPluginExtension
		get() = extensions.getByName<GITPluginExtension>(GITPluginExtension.NAME)

	// `: VCSExtension by current` is not possible because `current` is "lateinit"
	override val isAvailable: Boolean
		get() = current.isAvailable

	override val isAvailableQuick: Boolean
		get() = current.isAvailableQuick

	override val revision: String
		get() = current.revision

	override val revisionNumber: Int
		get() = current.revisionNumber

	override fun files(project: Project): FileCollection =
		current.files(project)

	companion object {

		internal const val NAME: String = "VCS"

		internal val Project.vcs: VCSPluginExtension
			get() = this.extensions.getByName<VCSPluginExtension>(NAME)
	}
}
