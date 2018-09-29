@file:Suppress("PackageDirectoryMismatch")

package net.twisterrob.gradle.test

import java.io.File
import java.lang.Math.random

fun File.createTestFileToCommit() = createTestFileToCommit(random().toString())

fun File.createTestFileToCommit(message: String): File {
	val hash = message.hashCode().toString(16).padStart(16, '0')
	return resolve("commit_$hash.commit").also { it.writeText(message) }
}

