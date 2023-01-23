package net.twisterrob.gradle.nagging

import org.gradle.api.Plugin
import org.gradle.api.invocation.Gradle

/**
 * Used from `init.gradle.kts` in test resources.
 */
class NaggingPlugin : Plugin<Gradle> {
	override fun apply(gradle: Gradle) {
		gradle.rootProject { rootProject ->
			// Groovy .ext === Kotlin .extensions.extraProperties === Kotlin DSL .extra
			// Based on https://stackoverflow.com/a/19269037/253468
			// Based on https://discuss.gradle.org/t/how-to-access-a-function-defined-in-init-gradle-in-build-script/6200/2

			// Access from build.gradle:
			// def doNotNagAbout = rootProject.ext["doNotNagAbout"]
			// doNotNagAbout("7.4.2", "^7\\.2\\.\\d+\$", "message")

			// Access from build.gradle.kts:
			// val doNotNagAbout = project.rootProject.extra["doNotNagAbout"] as (String, String, String) -> Unit
			// val doNotNagAbout = project.rootProject.extensions.extraProperties["doNotNagAbout"] as (String, String, String) -> Unit
			// doNotNagAbout("7.4.2", """^7\.2\.\d$""", "message")

			rootProject.extensions.extraProperties.set("doNotNagAbout", ::doNotNagAbout)
			rootProject.extensions.extraProperties.set("doNotNagAboutPattern", ::doNotNagAboutPattern)
		}
	}
}
