plugins {
	id("com.android.application") version "7.4.0"
	// TODEL Duplicate function hack from below, use plugin 0.15 variant.
	id("net.twisterrob.quality") version "0.14.1"
	id("example")
}

android {
	compileSdk = 33
	defaultConfig {
		minSdk = 14
		targetSdk = 33

		namespace = "net.twisterrob.quality.examples.release"
		applicationId = "net.twisterrob.quality.examples.release"
		versionCode = 1
		versionName = "1.0"
	}
}

repositories {
	google()
	mavenCentral()
}
