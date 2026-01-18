plugins {
	id("com.android.application") version "9.0.0"
	id("net.twisterrob.gradle.plugin.quality") version "0.19-SNAPSHOT"
}

android {
	compileSdk = 35
	defaultConfig {
		minSdk = 21
		targetSdk = 35

		namespace = "net.twisterrob.quality.examples.local"
		versionCode = 1
		versionName = "1.0"
	}
	lint {
		checkAllWarnings = true
		warningsAsErrors = true
		disable += listOf(
			// Project is using renovate to manage dependencies.
			"AndroidGradlePluginVersion",
			"GradleDependency",
			"NewerVersionAvailable",
			// Manual update for now.
			"MinSdkTooLow",
			"OldTargetApi",
		)
	}
}
