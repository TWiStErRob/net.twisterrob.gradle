plugins {
	id("net.twisterrob.gradle.build.module.gradle-plugin")
	id("net.twisterrob.gradle.build.publish")
	id("org.gradle.java-test-fixtures")
}

base.archivesName.set("twister-quality-pmd")
description = "PMD: PMD quality setup plugin for Gradle."

gradlePlugin {
	@Suppress("UnstableApiUsage", "StringLiteralDuplication")
	plugins {
		create("pmd") {
			id = "net.twisterrob.gradle.plugin.pmd"
			displayName = "PMD Convention Plugin"
			description = """
				TWiStErRob's Convention plugin for PMD.
				
				Features:
				 * Automatic setup of PMD configuration
				 * Conventional location: config/pmd/pmd.xml
			""".trimIndent()
			tags.set(setOf("conventions", "pmd"))
			implementationClass = "net.twisterrob.gradle.pmd.PmdPlugin"
		}
	}
}

dependencies {
	api(projects.common)

	compileOnly(libs.android.gradle)

	testImplementation(projects.test.internal)
	testImplementation(projects.compat.agpBase)

	testFixturesImplementation(projects.test.internal)
}

pullTestResourcesFrom(projects.test)
