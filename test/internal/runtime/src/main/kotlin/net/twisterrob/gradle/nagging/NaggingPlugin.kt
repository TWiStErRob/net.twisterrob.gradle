package net.twisterrob.gradle.nagging

import org.gradle.api.Plugin
import org.gradle.api.invocation.Gradle
import org.gradle.api.plugins.ExtensionAware

/**
 * Used from `init.gradle.kts` in test resources.
 */
class NaggingPlugin : Plugin<Gradle> {
	override fun apply(gradle: Gradle) {
		gradle.rootProject(::exposeDoNotNagAboutExtras)
		gradle.beforeSettings(::exposeDoNotNagAboutExtras)
	}

	/**
	 * Expose [doNotNagAboutStackForTest] and [doNotNagAboutPatternForTest] to build scripts.
	 *
	 * Groovy `.ext` === Kotlin `.extensions.extraProperties` === Kotlin DSL `.extra`.
	 * Based on [Function from init.gradle in build script](https://stackoverflow.com/a/19269037/253468).
	 * Based on [How to access a function defined in init.gradle in build script](https://discuss.gradle.org/t/6200/2)
	 *
	 * Examples are for `rootProject`, replace `rootProject` with `settings` for [org.gradle.api.initialization.Settings].
	 *
	 * Access from `build.gradle`:
	 * ```groovy
	 * def doNotNagAboutForTest = rootProject.ext["doNotNagAboutForTest"]
	 * doNotNagAboutForTest("7.4.2", "^7\\.2\\.\\d+\$", "message")
	 * ```
	 *
	 * Access from `build.gradle.kts`:
	 * ```kotlin
	 * val doNotNagAboutForTest = rootProject.extra["doNotNagAboutForTest"] as (String, String, String) -> Unit
	 * val doNotNagAboutForTest = rootProject.extensions.extraProperties["doNotNagAboutForTest"] as (String, String, String) -> Unit
	 * doNotNagAboutForTest("7.4.2", """^7\.2\.\d$""", "message")
	 * ```
	 *
	 * Similar functions need different signatures!
	 */
	private fun exposeDoNotNagAboutExtras(extensionAware: ExtensionAware) {
		extensionAware.extensions.extraProperties.set("doNotNagAboutForTest", ::doNotNagAboutForTest)
		extensionAware.extensions.extraProperties.set("doNotNagAboutStackForTest", ::doNotNagAboutStackForTest)
		extensionAware.extensions.extraProperties.set("doNotNagAboutPatternForTest", ::doNotNagAboutPatternForTest)
	}
}
