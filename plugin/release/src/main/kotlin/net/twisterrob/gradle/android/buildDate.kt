package net.twisterrob.gradle.android

import net.twisterrob.gradle.base.BasePlugin
import java.net.JarURLConnection
import java.time.Instant
import java.time.format.DateTimeFormatter

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
