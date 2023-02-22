plugins {
	id("com.android.application") version "7.4.1"
	// TODEL resolutionStrategy in settings.gradle.kts after changing to release 0.15
	// TODO change plugin name after changing to release 0.15
	// TODO enable doNotNagAbout below and make org.gradle.warning.mode=fail when updating to 0.15.
	id("net.twisterrob.quality") version "0.15"
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
