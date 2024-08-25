plugins {
	id("com.android.application") version "8.5.2"
	id("net.twisterrob.gradle.plugin.quality") version "0.18-SNAPSHOT"
}

android {
	compileSdk = 34
	defaultConfig {
		minSdk = 14
		targetSdk = 34

		namespace = "net.twisterrob.quality.examples.snapshot"
		versionCode = 1
		versionName = "1.0"
	}
}
