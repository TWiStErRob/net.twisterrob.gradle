package net.twisterrob.gradle.android

import org.gradle.api.file.RegularFileProperty
import java.nio.charset.Charset

fun RegularFileProperty.writeText(text: String, charset: Charset = Charsets.UTF_8) {
	this.get().asFile.also { if (!it.parentFile.exists()) check(it.parentFile.mkdirs()) }.writeText(text, charset)
}
