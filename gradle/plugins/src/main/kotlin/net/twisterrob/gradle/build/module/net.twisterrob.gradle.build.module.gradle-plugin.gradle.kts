import net.twisterrob.gradle.build.testing.InitScriptMetadataPlugin

plugins {
	id("org.gradle.java-gradle-plugin")
	id("org.gradle.java-test-fixtures")
	id("net.twisterrob.gradle.build.testing.plugin-under-test-metadata-extras")
	id("net.twisterrob.gradle.build.libraries")
}
plugins.apply(InitScriptMetadataPlugin::class)
