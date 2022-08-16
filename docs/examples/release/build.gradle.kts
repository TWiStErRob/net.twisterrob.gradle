plugins {
	id("com.android.application") version "7.3.0-rc01"
	id("net.twisterrob.quality") version "0.14.1"
}

android {
	compileSdk = 30
	defaultConfig {
		minSdk = 14
		targetSdk = 30

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
