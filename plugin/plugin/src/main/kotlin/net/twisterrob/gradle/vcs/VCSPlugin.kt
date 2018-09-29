package net.twisterrob.gradle.vcs

import net.twisterrob.gradle.base.BaseExposedPlugin
import net.twisterrob.gradle.kotlin.dsl.extensions
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.getByName

open class VCSPluginExtension : VCSExtension {

	var current: VCSExtension = DummyVcsExtension
		internal set

	val svn: SVNPluginExtension
		get() = extensions.getByName<SVNPluginExtension>("svn")
	val git: GITPluginExtension
		get() = extensions.getByName<GITPluginExtension>("git")

	// `: VCSExtension by current` is not possible because `current` is "lateinit"
	override val isAvailable get() = current.isAvailable
	override val isAvailableQuick get() = current.isAvailableQuick
	override val revision get() = current.revision
	override val revisionNumber get() = current.revisionNumber
}

class VCSPlugin : BaseExposedPlugin() {

	companion object {
		@JvmStatic
		fun VCSPluginExtension.whichVCS(): VCSExtension = when {
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

		val vcs = project.extensions.create("VCS", VCSPluginExtension::class.java)
		project.apply<SVNPlugin>()
		project.apply<GITPlugin>()
		vcs.current = vcs.whichVCS()
	}
}
