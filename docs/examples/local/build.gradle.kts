plugins {
	id("com.android.application") version "8.1.1"
	id("net.twisterrob.gradle.plugin.quality") version "0.17-SNAPSHOT"
}

android {
	compileSdk = 33
	defaultConfig {
		minSdk = 14
		targetSdk = 33

		namespace = "net.twisterrob.quality.examples.local"
		versionCode = 1
		versionName = "1.0"
	}
}
