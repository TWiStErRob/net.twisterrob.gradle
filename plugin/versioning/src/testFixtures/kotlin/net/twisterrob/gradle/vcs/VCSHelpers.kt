package net.twisterrob.gradle.vcs

import java.io.File
import kotlin.math.absoluteValue

fun File.createTestFileToCommit(): File =
	createTestFileToCommit(Math.random().toString())

fun File.createTestFileToCommit(message: String): File {
	val hash = message.hashCode().toHexString()
	return resolve("test_commit_$hash.txt").apply { writeText(message) }
}

private fun Int.toHexString(): String =
	this.absoluteValue.toString(@Suppress("MagicNumber") 16).padStart(@Suppress("MagicNumber") 8, '0')
