package net.twisterrob.gradle.vcs

import net.twisterrob.gradle.base.BasePlugin
import net.twisterrob.gradle.kotlin.dsl.extensions
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.errors.RepositoryNotFoundException
import org.eclipse.jgit.lib.AbbreviatedObjectId
import org.eclipse.jgit.lib.AnyObjectId
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.RepositoryCache
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.util.FS
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByName
import java.io.File
import java.io.FileNotFoundException

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

		internal const val NAME: String = "git"
	}

	internal lateinit var project: Project

	private inline fun <T> inRepo(block: Git.() -> T): T =
		inRepo(project.rootDir, block)

	override val isAvailableQuick: Boolean
		get() = project.rootDir.resolve(".git").exists()

	// 'git describe --always'.execute([], project.rootDir).waitFor() == 0
	override val isAvailable: Boolean
		get() {
			// Check more than just the presence of .git to lessen the possibility of detecting "git",
			// but not actually having a git repository.
			RepositoryCache.FileKey.resolve(project.rootDir, FS.DETECTED) ?: return false
			return try {
				// Actually try to open the repository now.
				inRepo { /* Just open, then close. */ }
				true
			} catch (_: FileNotFoundException) {
				// https://bugs.eclipse.org/bugs/show_bug.cgi?id=572617
				false
			} catch (_: RepositoryNotFoundException) {
				false
			}
		}

	// 'git rev-parse --short HEAD'.execute([], project.rootDir).text.trim()
	override val revision: String
		get() = inRepo {
			abbreviate(head).name()
		}

	// 'git rev-list --count HEAD'.execute([], project.rootDir).text.trim()
	override val revisionNumber: Int
		get() = inRepo {
			walk<Int> {
				isRetainBody = false
				markStart(parseCommit(head))
				return count()
			}
		}
}

private inline fun <T> inRepo(dir: File, block: Git.() -> T): T {
	val repo = Git.open(dir)
	return repo.use(block)
}

private val Git.head: ObjectId
	get() = this.repository.resolve("HEAD")

private inline fun <T> Git.walk(block: RevWalk.() -> T): T {
	val walk = RevWalk(this.repository)
	return walk.use(block)
}

private fun Git.abbreviate(objectId: AnyObjectId): AbbreviatedObjectId =
	this.repository.newObjectReader().use { reader ->
		reader.abbreviate(objectId)
	}
