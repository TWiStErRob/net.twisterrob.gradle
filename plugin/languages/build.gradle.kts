plugins {
	id("net.twisterrob.gradle.build.module.gradle-plugin")
	id("net.twisterrob.gradle.build.publish")
}

base.archivesName = "twister-convention-languages"
description = "Languages Convention Plugin: Gradle Plugin to apply Java and Kotlin conventions."

gradlePlugin {
	@Suppress("UnstableApiUsage", "detekt.StringLiteralDuplication")
	plugins {
		create("java") {
			id = "net.twisterrob.gradle.plugin.java"
			displayName = "Java Convention Plugin"
			description = """
				TWiStErRob's Convention plugin for Java modules.
				Various defaults and hacks to make development easier.
			""".trimIndent()
			tags = setOf("conventions", "java")
			implementationClass = "net.twisterrob.gradle.java.JavaPlugin"
			deprecateId(project, "net.twisterrob.java")
		}
		create("javaLibrary") {
			id = "net.twisterrob.gradle.plugin.java-library"
			displayName = "Java Library Convention Plugin"
			description = """
				TWiStErRob's Convention plugin for Java library modules.
			""".trimIndent()
			tags = setOf("conventions", "java")
			implementationClass = "net.twisterrob.gradle.java.JavaLibPlugin"
			deprecateId(project, "net.twisterrob.java-library")
		}
		create("kotlin") {
			id = "net.twisterrob.gradle.plugin.kotlin"
			displayName = "Kotlin Convention Plugin"
			description = """
				TWiStErRob's Convention plugin for Kotlin modules.
			""".trimIndent()
			tags = setOf("conventions", "kotlin")
			implementationClass = "net.twisterrob.gradle.kotlin.KotlinPlugin"
			deprecateId(project, "net.twisterrob.kotlin")
		}
	}
}

dependencies {
	implementation(gradleApi())
	api(projects.plugin.base)
	compileOnly(libs.android.gradle)

	testImplementation(projects.test.internal)
	testImplementation(testFixtures(projects.plugin.base))

	// This plugin is part of the net.twisterrob.gradle.plugin.android-app plugin, not designed to work on its own.
	testInjectedPluginClasspath(projects.plugin)
	testInjectedPluginClasspath(libs.android.gradle) {
		version { require(property("net.twisterrob.test.android.pluginVersion").toString()) }
	}
	testInjectedPluginClasspath(libs.kotlin.gradle) {
		version { require(property("net.twisterrob.test.kotlin.pluginVersion").toString()) }
	}
}
