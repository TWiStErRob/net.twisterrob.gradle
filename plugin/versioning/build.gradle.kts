import net.twisterrob.gradle.build.publishing.createJavaVariant

plugins {
	id("net.twisterrob.gradle.build.module.gradle-plugin")
	id("net.twisterrob.gradle.build.publish")
}

base.archivesName = "twister-convention-versioning"
description = "Versioning Convention Plugin: Gradle Plugin to set up versioning through properties and DSL."

gradlePlugin {
	@Suppress("UnstableApiUsage", "detekt.StringLiteralDuplication")
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
			tags = setOf("conventions", "android", "versioning", "git", "svn", "vcs")
			implementationClass = "net.twisterrob.gradle.vcs.VCSPlugin"
			deprecateId(project, "net.twisterrob.vcs")
		}
	}
}

val java17 by sourceSets.creating

java {
	registerFeature("java17") {
		usingSourceSet(java17)
		capability(group.toString(), project.name, version.toString())
	}
}

val java17RuntimeElements by configurations.existing {
	attributes {
		attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 17)
	}
}

val java17ApiElements by configurations.existing {
	attributes {
		attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 17)
	}
}
val java17RuntimeOnly by configurations

publishing {
	publications {
		named<MavenPublication>("pluginMaven") {
			suppressPomMetadataWarningsFor("java17ApiElements")
			suppressPomMetadataWarningsFor("java17RuntimeElements")
		}
	}
}

dependencies {
	implementation(gradleApi())
	implementation(projects.plugin.base)
	implementation(projects.compat.gradle)
	implementation(projects.compat.agpBase)
	implementation(projects.compat.agp)
	implementation(libs.svnkit)
	implementation(libs.svnkit.cli)
	compileOnly(libs.android.gradle)
	implementation(libs.jgit11) {
		because("JGit 6.10.0 is the last version to support Java 11. JGit 7.x requires Java 17.")
		versionConstraint.rejectedVersions.add("[7.0,)")
	}
	java17RuntimeOnly(libs.jgit17) {
		because("JGit 7.x is the first version to require Java 17.")
		versionConstraint.rejectedVersions.add("(,7.0)")
	}

	// This plugin is part of the net.twisterrob.gradle.plugin.android-app plugin, not designed to work on its own.
	runtimeOnly(projects.plugin)

	testImplementation(projects.test.internal)
	testImplementation(testFixtures(projects.plugin.base))
	testInjectedPluginClasspath(libs.android.gradle) {
		version { require(property("net.twisterrob.test.android.pluginVersion").toString()) }
	}

	testFixturesApi(libs.svnkit)
}
