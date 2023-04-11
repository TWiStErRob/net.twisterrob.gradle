import net.twisterrob.gradle.build.compilation.JavaCompatibilityPlugin
import net.twisterrob.gradle.build.detekt.DetektPlugin
import net.twisterrob.gradle.build.testing.InitScriptMetadataPlugin

plugins {
	id("org.gradle.java-gradle-plugin")
	id("net.twisterrob.gradle.build.testing.plugin-under-test-metadata-extras")
	id("org.jetbrains.kotlin.jvm")
	//id("org.gradle.java-test-fixtures")
}
plugins.apply(InitScriptMetadataPlugin::class)
plugins.apply(JavaCompatibilityPlugin::class)
plugins.apply(DetektPlugin::class)
