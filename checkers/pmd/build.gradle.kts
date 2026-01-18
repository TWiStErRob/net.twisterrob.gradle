import net.twisterrob.gradle.build.testing.pullTestResourcesFrom

plugins {
	id("net.twisterrob.gradle.build.module.gradle-plugin")
	id("net.twisterrob.gradle.build.publish")
}

base.archivesName = "twister-quality-pmd"
description = "PMD: PMD quality setup plugin for Gradle."

gradlePlugin {
	@Suppress("detekt.StringLiteralDuplication")
	plugins {
		register("pmd") {
			id = "net.twisterrob.gradle.plugin.pmd"
			displayName = "PMD Convention Plugin"
			description = """
				TWiStErRob's Convention plugin for PMD.
				
				Features:
				 * Automatic setup of PMD configuration
				 * Conventional location: config/pmd/pmd.xml
			""".trimIndent()
			tags = setOf("conventions", "pmd")
			implementationClass = "net.twisterrob.gradle.pmd.PmdPlugin"
			deprecateId(project, "net.twisterrob.pmd")
		}
	}
}

dependencies {
	api(projects.common)

	compileOnly(libs.android.gradle)
	implementation(projects.compat.agp)

	testImplementation(projects.test.internal)
	testInjectedPluginClasspath(libs.android.gradle) {
		version { require(property("net.twisterrob.test.android.pluginVersion").toString()) }
	}

	testFixturesImplementation(projects.test.internal)
}

pullTestResourcesFrom(projects.test)
