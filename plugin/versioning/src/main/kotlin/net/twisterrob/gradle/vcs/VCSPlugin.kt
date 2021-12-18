package net.twisterrob.gradle.vcs

import net.twisterrob.gradle.base.BaseExposedPlugin
import net.twisterrob.gradle.kotlin.dsl.extensions
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByName

open class VCSPluginExtension : VCSExtension {

	companion object {

		internal const val NAME: String = "VCS"
	}

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
}

class VCSPlugin : BaseExposedPlugin() {

	companion object {

		@JvmStatic
		fun VCSPluginExtension.whichVCS(): VCSExtension =
			when {
				current != DummyVcsExtension -> current // already determined
				svn.isAvailableQuick -> svn
				git.isAvailableQuick -> git
				svn.isAvailable -> svn
				git.isAvailable -> git
				else -> DummyVcsExtension
			}
	}

	override fun apply(target: Project) {
		super.apply(target)

		val vcs = project.extensions.create<VCSPluginExtension>(VCSPluginExtension.NAME)
		project.apply<SVNPlugin>()
		project.apply<GITPlugin>()
		vcs.current = vcs.whichVCS()
	}
}
