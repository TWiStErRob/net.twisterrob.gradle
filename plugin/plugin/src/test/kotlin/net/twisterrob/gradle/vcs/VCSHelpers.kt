package net.twisterrob.gradle.vcs

import java.io.File

internal fun File.createTestFileToCommit(): File =
	createTestFileToCommit(Math.random().toString())

internal fun File.createTestFileToCommit(message: String): File {
	val hash = message.hashCode().toString(16).padStart(8, '0')
	return resolve("commit_$hash.commit").also { it.writeText(message) }
}
