package net.twisterrob.gradle.vcs

import net.twisterrob.gradle.base.BasePlugin
import net.twisterrob.gradle.kotlin.dsl.extensions
import org.ajoberstar.grgit.Grgit
import org.eclipse.jgit.errors.RepositoryNotFoundException
import org.eclipse.jgit.revwalk.RevWalk
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByName

class GITPlugin : BasePlugin() {

	override fun apply(target: Project) {
		super.apply(target)

		val git = project
			.extensions.getByName<VCSPluginExtension>(VCSPluginExtension.NAME)
			.extensions.create<GITPluginExtension>(GITPluginExtension.NAME)
		git.project = project // TODO better solution
	}
}

open class GITPluginExtension : VCSExtension {

	companion object {
		internal const val NAME = "git"
	}

	internal lateinit var project: Project

	@Suppress("DEPRECATION")
	private fun open(): Grgit = Grgit.open(project.rootDir)

	override val isAvailableQuick: Boolean
		get() = project.rootDir.resolve(".git").exists()

	// 'git describe --always'.execute([], project.rootDir).waitFor() == 0
	override val isAvailable: Boolean
		get() = try {
			open().close()
			true
		} catch (_: RepositoryNotFoundException) {
			false
		}
	// 'git rev-parse --short HEAD'.execute([], project.rootDir).text.trim()
	override val revision: String
		get() = open().use { git -> git.head().abbreviatedId }

	// 'git rev-list --count HEAD'.execute([], project.rootDir).text.trim()
	override val revisionNumber: Int
		get() = open().use { git ->
			val repository = git.repository.jgit.repository
			RevWalk(repository).use { walk ->
				walk.isRetainBody = false
				walk.markStart(walk.parseCommit(repository.resolve("HEAD")))
				return walk.count()
			}
		}
}
