plugins {
	id("net.twisterrob.gradle.build.module.gradle-plugin")
	id("net.twisterrob.gradle.build.publish")
}

base.archivesName = "twister-convention-base"
description = "Utilities: utility functions and classes to write convention plugins."

gradlePlugin {
	@Suppress("UnstableApiUsage", "detekt.StringLiteralDuplication")
	plugins {
		create("root") {
			id = "net.twisterrob.gradle.plugin.root"
			displayName = "Gradle Root Convention Plugin"
			description = """
				TWiStErRob's Convention plugin for Gradle rootProject modules.
				
				Features:
				 * Adds a debugWrapper task to generate gradled.bat debug helper.
			""".trimIndent()
			tags = setOf("conventions", "rootProject")
			implementationClass = "net.twisterrob.gradle.root.RootPlugin"
			deprecateId(project, "net.twisterrob.root")
		}
	}
}

dependencies {
	implementation(gradleApi())
	api(projects.common)
	compileOnly(libs.android.gradle)
	// SdkConstants.FD_INTERMEDIATES
	compileOnly(libs.android.tools.common)
	compileOnly(libs.annotations.jetbrains)
	implementation(projects.compat.gradle)
	implementation(projects.compat.agpBase)
	api(projects.compat.agp)

	testImplementation(projects.test.internal)

	testFixturesImplementation(projects.compat.agpBase)
	testFixturesImplementation(projects.test.internal)
	testFixturesCompileOnly(projects.test)
	testFixturesApi(libs.dexMemberList)
	// GradleTestHelpersKt.hasDevices uses com.android.ddmlib.AndroidDebugBridge.
	testFixturesImplementation(libs.android.tools.ddmLib)
}
