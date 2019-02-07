package net.twisterrob.gradle

import net.twisterrob.gradle.base.BasePlugin
import java.net.JarURLConnection
import java.time.Instant
import java.time.format.DateTimeFormatter

val builtDate: Instant by lazy {
	val aClassInJar = BasePlugin::class.java
	val res = aClassInJar.getResource(aClassInJar.simpleName + ".class")!!
	val url = res.openConnection()!!
	when {
		url is JarURLConnection -> {
			val mf = url.manifest!!
			val date = mf.mainAttributes.getValue("Built-Date")!!
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
