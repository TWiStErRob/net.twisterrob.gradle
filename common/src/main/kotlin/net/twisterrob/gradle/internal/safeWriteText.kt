package net.twisterrob.gradle.internal

import org.gradle.api.file.RegularFileProperty
import java.io.File
import java.nio.charset.Charset

fun RegularFileProperty.safeWriteText(text: String, charset: Charset = Charsets.UTF_8) {
	this.get().asFile
		.also { it.parentFile.ensureParentDirs() }
		.writeText(text, charset)
}

private fun File.ensureParentDirs() {
	if (!this.parentFile.exists()) {
		check(this.parentFile.mkdirs()) {
			"Failed to create directory for ${this} -> ${this.parentFile.absolutePath}."
		}
	}
}
