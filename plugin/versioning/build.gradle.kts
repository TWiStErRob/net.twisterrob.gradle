plugins {
	id("net.twisterrob.gradle.build.module.gradle-plugin")
	id("net.twisterrob.gradle.build.publish")
	id("org.gradle.java-test-fixtures")
}

base.archivesName.set("twister-convention-versioning")
description = "Versioning Convention Plugin: Gradle Plugin to set up versioning through properties and DSL."

gradlePlugin {
	@Suppress("UnstableApiUsage", "StringLiteralDuplication")
	plugins {
		create("vcs") {
			id = "net.twisterrob.gradle.plugin.vcs"
			displayName = "Versioning Convention Plugin"
			description = """
				TWiStErRob's Convention plugin for Version Control.
				
				Features:
				 * Auto-detect GIT and SVN version control.
				 * Expose VCS information such as revision and revisionNumber.
				 * Auto-version Android artifacts (versionCode and versionName) from version.properties and VCS.
				 * Rename Android APK to contain more information:
				   `{applicationId}@{versionCode}-v{versionName}+{variant}.apk`
			""".trimIndent()
			tags.set(setOf("conventions", "android", "versioning", "git", "svn", "vcs"))
			implementationClass = "net.twisterrob.gradle.vcs.VCSPlugin"
			deprecateId(project, "net.twisterrob.vcs")
		}
	}
}

dependencies {
	implementation(gradleApiWithoutKotlin())
	implementation(projects.plugin.base)
	implementation(projects.compat.gradle)
	implementation(projects.compat.agpBase)
	implementation(projects.compat.agp)
	implementation(libs.svnkit)
	implementation(libs.svnkit.cli)
	implementation(libs.jgit)
	compileOnly(libs.android.gradle)

	// This plugin is part of the net.twisterrob.android-app plugin, not designed to work on its own.
	runtimeOnly(projects.plugin)

	testImplementation(projects.test.internal)
	testImplementation(testFixtures(projects.plugin.base))

	testFixturesApi(libs.svnkit)
	testFixturesApi(libs.jgit)
}
