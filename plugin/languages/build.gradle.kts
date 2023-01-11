plugins {
	id("net.twisterrob.gradle.build.module.gradle-plugin")
	id("net.twisterrob.gradle.build.publish")
}

base.archivesName.set("twister-convention-languages")
description = "Languages Convention Plugin: Gradle Plugin to apply Java and Kotlin conventions."

gradlePlugin {
	@Suppress("UnstableApiUsage", "StringLiteralDuplication")
	plugins {
		create("java") {
			id = "net.twisterrob.java"
			displayName = "Java Convention Plugin"
			description = """
				TWiStErRob's Convention plugin for Java modules.
				Various defaults and hacks to make development easier.
			""".trimIndent()
			tags.set(setOf("conventions", "java"))
			implementationClass = "net.twisterrob.gradle.java.JavaPlugin"
		}
		create("javaLibrary") {
			id = "net.twisterrob.java-library"
			displayName = "Java Library Convention Plugin"
			description = """
				TWiStErRob's Convention plugin for Java library modules.
			""".trimIndent()
			tags.set(setOf("conventions", "java"))
			implementationClass = "net.twisterrob.gradle.java.JavaLibPlugin"
		}
		create("kotlin") {
			id = "net.twisterrob.kotlin"
			displayName = "Kotlin Convention Plugin"
			description = """
				TWiStErRob's Convention plugin for Kotlin modules.
			""".trimIndent()
			tags.set(setOf("conventions", "kotlin"))
			implementationClass = "net.twisterrob.gradle.kotlin.KotlinPlugin"
		}
	}
}

dependencies {
	implementation(gradleApiWithoutKotlin())
	api(projects.plugin.base)
	compileOnly(libs.android.gradle)

	// This plugin is part of the net.twisterrob.android-app plugin, not designed to work on its own.
	runtimeOnly(projects.plugin)

	testImplementation(projects.test.internal)
	testImplementation(testFixtures(projects.plugin.base))
}
