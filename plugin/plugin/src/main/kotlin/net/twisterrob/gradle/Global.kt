package net.twisterrob.gradle

import net.twisterrob.gradle.base.BasePlugin
import org.gradle.api.file.RegularFileProperty
import java.net.JarURLConnection
import java.nio.charset.Charset
import java.time.Instant
import java.time.format.DateTimeFormatter
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

val builtDate: Instant by lazy {
	val aClassInJar = BasePlugin::class.java
	val aClassName = aClassInJar.simpleName + ".class"
	val res = aClassInJar.getResource(aClassName) ?: error("Cannot find class file ${aClassName}")
	val url = res.openConnection() ?: error("Cannot open ${res}")
	when {
		url is JarURLConnection -> {
			val mf = url.manifest ?: error("Cannot find manifest in ${url.jarFileURL}")
			val date = mf.mainAttributes.getValue("Built-Date")
				?: error("Build-Date attribute not present in manifest\n${url.manifest.mainAttributes.toMap()}")
			Instant.from(DateTimeFormatter.ISO_INSTANT.parse(date))
		}

		// symbol is declared in module 'java.base' which does not export package 'sun.net.www.protocol.file'
		url::class.java == Class.forName("sun.net.www.protocol.file.FileURLConnection") ->
			// e.g. when running tests and .class is in .../classes/
			Instant.now()
		else ->
			error("Unknown URL type ${url::class.java}")
	}
}

fun <R> systemProperty(name: String) = object : ReadWriteProperty<R, String> {
	override fun getValue(thisRef: R, property: KProperty<*>): String =
		System.getProperty(name)
			?: error("Cannot find System property value for ${name}.")

	override fun setValue(thisRef: R, property: KProperty<*>, value: String) {
		System.setProperty(name, value)
	}
}

fun RegularFileProperty.writeText(text: String, charset: Charset = Charsets.UTF_8) {
	this.get().asFile.also { if (!it.parentFile.exists()) check(it.parentFile.mkdirs()) }.writeText(text, charset)
}
