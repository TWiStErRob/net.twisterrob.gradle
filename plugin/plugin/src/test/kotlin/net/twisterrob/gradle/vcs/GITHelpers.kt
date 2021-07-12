package net.twisterrob.gradle.vcs

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.revwalk.RevCommit
import java.io.File

fun createGitRepository(repoDir: File): Git =
	Git
		.init()
		.apply {
			setDirectory(repoDir)
		}
		.call()
		.also { result -> println("Repository created at ${result}") }

fun Git.doCommitSingleFile(file: File, message: String): RevCommit {
	val relativePath = file.relativeTo(this@doCommitSingleFile.repository.directory)
	this
		.add()
		.apply {
			addFilepattern(relativePath.toString())
		}
		.call()
		.also { println("Added $relativePath") }

	return this
		.commit()
		.apply {
			setMessage(message)
		}
		.call()
		.also { println("Committed revision ${it.id}: ${it.fullMessage}") }
}

inline fun git(repoDir: File, block: Git.() -> Unit) {
	val repo = createGitRepository(repoDir)
	repo.use {
		block(it)
	}
}
