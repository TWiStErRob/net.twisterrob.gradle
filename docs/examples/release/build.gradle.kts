plugins {
	id("com.android.application") version "8.2.1"
	id("net.twisterrob.gradle.plugin.quality") version "0.16"
}

android {
	compileSdk = 34
	defaultConfig {
		minSdk = 14
		targetSdk = 34

		namespace = "net.twisterrob.quality.examples.release"
		versionCode = 1
		versionName = "1.0"
	}
}
