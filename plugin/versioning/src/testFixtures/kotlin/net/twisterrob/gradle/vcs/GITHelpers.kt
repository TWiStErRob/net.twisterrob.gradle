@file:Suppress("ForbiddenMethodCall")

package net.twisterrob.gradle.vcs

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.GpgConfig.GpgFormat
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.revwalk.RevCommit
import java.io.File
import java.util.concurrent.Callable

inline fun git(repoDir: File, block: Git.() -> Unit) {
	val repo = createGitRepository(repoDir)
	repo.use(block)
}

fun createGitRepository(repoDir: File): Git =
	Git
		.init()
		.call {
			setDirectory(repoDir)
		}
		.apply {
			// The following in .gitconfig blows up:
			// ```
			// [gpg]
			//     format = ssh
			// ```
			// with IllegalArgumentException: Invalid value: gpg.format=ssh
			// at org.eclipse.jgit.lib.GpgConfig.<init>(GpgConfig.java:86)
			// in JGIT 6.6.0.202305301015-r
			repository.config.setEnum("gpg", null,  "format", GpgFormat.OPENPGP)
		}
		.also { result -> println("Repository ${repoDir} created at ${result}") }

fun Git.doCommitSingleFile(file: File, message: String): RevCommit {
	val relativePath = file.relativeTo(this.repository.directory)
	this
		.add()
		.call {
			addFilepattern(relativePath.toString())
		}
		.also { println("Added $relativePath") }

	return this
		.commit()
		.call {
			setMessage(message)
		}
		.also { commit -> println("Committed revision ${commit.id}: ${commit.fullMessage}") }
}

fun Git.doCheckout(objectId: ObjectId): Ref? =
	this.doCheckout(objectId.name)

fun Git.doCheckout(name: String): Ref? {
	return this
		.checkout()
		.call {
			setName(name)
		}
		.also { ref -> println("Checked out ${name} as ${ref?.objectId ?: name}") }
}

private fun <R, T : Callable<R>> T.call(block: T.() -> Unit): R =
	this.apply(block).call()
