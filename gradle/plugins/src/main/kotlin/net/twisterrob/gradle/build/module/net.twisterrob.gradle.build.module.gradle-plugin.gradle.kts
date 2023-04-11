import net.twisterrob.gradle.build.testing.InitScriptMetadataPlugin

plugins {
	id("org.gradle.java-gradle-plugin")
	id("net.twisterrob.gradle.build.testing.plugin-under-test-metadata-extras")
	id("net.twisterrob.gradle.build.libraries")
	//id("org.gradle.java-test-fixtures")
}
plugins.apply(InitScriptMetadataPlugin::class)
