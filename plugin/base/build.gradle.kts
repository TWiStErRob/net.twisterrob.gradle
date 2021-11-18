plugins {
	kotlin
	`java-library`
	`java-test-fixtures`
	id("java-gradle-plugin")
	id("net.twisterrob.gradle.build.publishing")
}

base.archivesName.set("twister-convention-plugins-common")
description = "Utilities: utility functions and classes to write convention plugins."

gradlePlugin {
	plugins {
	}
}

dependencies {
	implementation(gradleApiWithoutKotlin())
	compileOnly(libs.android.gradle)
	compileOnly(libs.annotations.jetbrains)

	testImplementation(projects.test.internal)

	testFixturesImplementation(projects.test.internal)
	testFixturesImplementation(projects.common)
	testFixturesApi(libs.dexMemberList)
	// GradleTestHelpersKt.hasDevices uses com.android.ddmlib.AndroidDebugBridge.
	testFixturesImplementation(libs.android.tools.ddmLib)
}
