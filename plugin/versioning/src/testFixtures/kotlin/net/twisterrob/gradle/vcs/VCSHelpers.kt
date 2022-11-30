package net.twisterrob.gradle.vcs

import java.io.File
import kotlin.math.absoluteValue

fun File.createTestFileToCommit(): File =
	createTestFileToCommit(Math.random().toString())

fun File.createTestFileToCommit(message: String): File {
	val hash = message.hashCode().absoluteValue.toString(16).padStart(8, '0')
	return resolve("test_commit_$hash.txt").apply { writeText(message) }
}
