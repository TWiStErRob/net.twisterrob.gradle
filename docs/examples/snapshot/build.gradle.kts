plugins {
	id("com.android.application") version "7.4.2"
	id("net.twisterrob.gradle.plugin.quality") version "0.16-SNAPSHOT"
}

android {
	compileSdk = 33
	defaultConfig {
		minSdk = 14
		targetSdk = 33

		namespace = "net.twisterrob.quality.examples.snapshot"
		versionCode = 1
		versionName = "1.0"
	}
}
