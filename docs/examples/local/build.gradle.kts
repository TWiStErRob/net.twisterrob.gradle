plugins {
	id("com.android.application") version "7.1.1"
	id("net.twisterrob.quality") version "0.15-SNAPSHOT"
}

android {
	compileSdk = 30
	defaultConfig {
		minSdk = 14
		targetSdk = 30

		applicationId = "net.twisterrob.quality.examples.local"
		versionCode = 1
		versionName = "1.0"
	}
}

repositories {
	google()
	mavenCentral()
}
