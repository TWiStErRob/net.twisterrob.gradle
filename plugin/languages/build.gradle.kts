plugins {
	id("org.gradle.java-gradle-plugin")
	id("org.jetbrains.kotlin.jvm")
	id("net.twisterrob.gradle.build.publishing")
	id("net.twisterrob.gradle.build.detekt")
}

base.archivesName.set("twister-convention-languages")
description = "Languages Convention Plugin: Gradle Plugin to apply Java and Kotlin conventions."

gradlePlugin {
	plugins {
		create("net.twisterrob.java") {
			id = "net.twisterrob.java"
			implementationClass = "net.twisterrob.gradle.java.JavaPlugin"
		}
		create("net.twisterrob.java-library") {
			id = "net.twisterrob.java-library"
			implementationClass = "net.twisterrob.gradle.java.JavaLibPlugin"
		}
		create("net.twisterrob.kotlin") {
			id = "net.twisterrob.kotlin"
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
