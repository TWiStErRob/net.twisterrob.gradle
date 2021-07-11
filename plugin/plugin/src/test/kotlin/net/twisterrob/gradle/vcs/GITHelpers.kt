package net.twisterrob.gradle.vcs

import org.ajoberstar.grgit.Commit
import org.ajoberstar.grgit.Grgit
import java.io.File

fun createGitRepository(repoDir: File): Grgit =
	Grgit.init {
		it.dir = repoDir
	}
	.also { result -> println("Repository created at ${result.repository}") }

fun Grgit.doCommitSingleFile(file: File, message: String): Commit {
	val relativePath = file.relativeTo(this@doCommitSingleFile.repository.rootDir)
	add {
		it.patterns = setOf(relativePath.toString())
	}.also { println("Added $relativePath") }

	return commit {
		it.message = message
	}.also { println("Committed revision ${it.id}: ${it.fullMessage}") }
}

inline fun git(repoDir: File, block: Grgit.() -> Unit) {
	val repo = createGitRepository(repoDir)
	repo.use {
		block(it)
	}
}
