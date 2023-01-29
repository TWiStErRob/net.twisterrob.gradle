plugins {
	id("com.android.application") version "7.4.0"
	// TODEL resolutionStrategy in settings.gradle.kts after changing to release 0.15
	// TODO change plugin name after changing to release 0.15
	// TODO enable doNotNagAbout below and make org.gradle.warning.mode=fail when updating to 0.15.
	id("net.twisterrob.quality") version "0.14.1"
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

// TODEL https://issuetracker.google.com/issues/264177800
if (com.android.Version.ANDROID_GRADLE_PLUGIN_VERSION < "7.4.1") {
	val gradleVersion: String = GradleVersion.current().version
//	net.twisterrob.gradle.doNotNagAbout(
//		"The Report.destination property has been deprecated. " +
//				"This is scheduled to be removed in Gradle 9.0. " +
//				"Please use the outputLocation property instead. " +
//				"See https://docs.gradle.org/${gradleVersion}/dsl/org.gradle.api.reporting.Report.html#org.gradle.api.reporting.Report:destination for more details.",
//		"at com.android.build.gradle.tasks.factory.AndroidUnitTest\$CreationAction.configure"
//	)
} else {
	error("AGP major version changed, review hack.")
}
