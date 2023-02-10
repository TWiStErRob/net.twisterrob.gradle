plugins {
	id("net.twisterrob.android-app") version "0.15.1-SNAPSHOT"
	id("com.android.application") version "7.4.1"
}

android {
	compileSdk = 33
	defaultConfig {
		minSdk = 14
		targetSdk = 33

		namespace = "net.twisterrob.quality.examples.local"
		applicationId = "net.twisterrob.quality.examples.local"
		versionCode = 1
		versionName = "1.0"
	}
}

repositories {
	google()
	mavenCentral()
}
