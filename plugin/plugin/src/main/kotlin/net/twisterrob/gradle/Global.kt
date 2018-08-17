package net.twisterrob.gradle

import net.twisterrob.gradle.base.BasePlugin
import sun.net.www.protocol.file.FileURLConnection
import java.net.JarURLConnection
import java.text.SimpleDateFormat
import java.util.*

val builtDate: Date by lazy {
	val aClassInJar = BasePlugin::class.java
	val res = aClassInJar.getResource(aClassInJar.simpleName + ".class")!!
	val url = res.openConnection()!!
	when (url) {
		is JarURLConnection -> {
			val mf = url.manifest!!
			val date = mf.mainAttributes.getValue("Built-Date")!!
			SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(date)
		}

		is FileURLConnection -> Date() // e.g. when running tests and .class is in .../classes/
		else -> error("Unknown URL type ${url::class.java}")
	}
}
