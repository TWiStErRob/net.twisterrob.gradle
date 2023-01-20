plugins {
	// Note: careful, the version number is repeated in settings.gradle.kts.
	id("com.android.application") version "7.4.0"
	id("net.twisterrob.quality") version "0.15-SNAPSHOT"
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

// TODEL https://issuetracker.google.com/issues/264177800
if (com.android.Version.ANDROID_GRADLE_PLUGIN_VERSION < "7.4.1") {
	val gradleVersion: String = GradleVersion.current().version
	net.twisterrob.gradle.doNotNagAbout(
		"The Report.destination property has been deprecated. " +
				"This is scheduled to be removed in Gradle 9.0. " +
				"Please use the outputLocation property instead. " +
				"See https://docs.gradle.org/${gradleVersion}/dsl/org.gradle.api.reporting.Report.html#org.gradle.api.reporting.Report:destination for more details."
	)
} else {
	error("AGP major version changed, review hack.")
}
