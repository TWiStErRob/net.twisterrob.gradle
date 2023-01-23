package net.twisterrob.gradle.nagging

internal val agpVersion: String = System.getProperty("net.twisterrob.test.android.pluginVersion")
	?: error("Property 'net.twisterrob.test.android.pluginVersion' is not set.")
