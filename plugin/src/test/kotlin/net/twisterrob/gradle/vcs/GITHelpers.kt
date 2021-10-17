package net.twisterrob.gradle.vcs

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.revwalk.RevCommit
import java.io.File
import java.util.concurrent.Callable

inline fun git(repoDir: File, block: Git.() -> Unit) {
	val repo = createGitRepository(repoDir)
	repo.use(block)
}

fun createGitRepository(repoDir: File): Git =
	Git
		.init().call {
			setDirectory(repoDir)
		}
		.also { result -> println("Repository created at ${result}") }

fun Git.doCommitSingleFile(file: File, message: String): RevCommit {
	val relativePath = file.relativeTo(this.repository.directory)
	this
		.add().call {
			addFilepattern(relativePath.toString())
		}
		.also { println("Added $relativePath") }

	return this
		.commit().call {
			setMessage(message)
		}
		.also { println("Committed revision ${it.id}: ${it.fullMessage}") }
}

private fun <R, T : Callable<R>> T.call(block: T.() -> Unit): R =
	this.apply(block).call()
