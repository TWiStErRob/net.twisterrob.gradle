plugins {
	id("com.android.application") version "7.3.1"
	id("net.twisterrob.quality") version "0.15-SNAPSHOT"
}

android {
	compileSdk = 33
	defaultConfig {
		minSdk = 14
		targetSdk = 33

		namespace = "net.twisterrob.quality.examples.snapshot"
		applicationId = "net.twisterrob.quality.examples.snapshot"
		versionCode = 1
		versionName = "1.0"
	}
}

repositories {
	google()
	mavenCentral()
}
