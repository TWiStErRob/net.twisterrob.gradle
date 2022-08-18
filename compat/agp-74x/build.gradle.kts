plugins {
	kotlin
	`java-library`
	id("net.twisterrob.gradle.build.publishing")
}

base.archivesName.set("twister-compat-agp-7.4.x")
description = "AGP Compatibility 7.4.x: Compatibility layer for Android Gradle Plugin 7.4.x."

dependencies {
	implementation(gradleApiWithoutKotlin())
	compileOnly(libs.android.gradle.v74x)
}

plugins.withId("java") {
	// libs.android.gradle.v74x declares `org.gradle.jvm.version=11` as an attribute,
	// which fails to resolve when using Java 8.
	// Details: `gradlew :compat:agp-74x:dependencyInsight --dependency=com.android.tools.build:gradle`
	val java = extensions.getByName<JavaPluginExtension>("java")
	java.sourceCompatibility = JavaVersion.VERSION_11
	java.targetCompatibility = JavaVersion.VERSION_11
}
