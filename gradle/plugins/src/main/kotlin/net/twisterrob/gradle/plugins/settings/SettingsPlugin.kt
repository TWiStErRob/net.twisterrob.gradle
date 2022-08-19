package net.twisterrob.gradle.plugins.settings

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.gradleEnterprise

class SettingsPlugin : Plugin<Settings> {
	override fun apply(settings: Settings) {
		settings.plugins.apply("com.gradle.enterprise")
		settings.gradleEnterprise {
			buildScan {
				termsOfServiceUrl = "https://gradle.com/terms-of-service"
				termsOfServiceAgree = "yes"
			}
		}
	}
}
