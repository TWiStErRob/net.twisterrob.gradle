package net.twisterrob.gradle.vcs

import net.twisterrob.gradle.common.BaseExposedPlugin
import net.twisterrob.gradle.vcs.git.GITPlugin
import net.twisterrob.gradle.vcs.svn.SVNPlugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create

@Suppress("detekt.UnnecessaryAbstractClass") // Gradle convention.
abstract class VCSPlugin : BaseExposedPlugin() {

	override fun apply(target: Project) {
		super.apply(target)

		val vcs = project.extensions.create<VCSPluginExtension>(VCSPluginExtension.NAME)
		project.apply<SVNPlugin>()
		project.apply<GITPlugin>()
		vcs.current = vcs.whichVCS()
	}

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
}
