plugins {
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

// TODEL https://issuetracker.google.com/issues/252848749
if (com.android.Version.ANDROID_GRADLE_PLUGIN_VERSION < "7.4") {
	val loggerFactory: org.slf4j.ILoggerFactory = org.slf4j.LoggerFactory.getILoggerFactory()
	val addNoOpLogger: java.lang.reflect.Method = loggerFactory.javaClass
		.getDeclaredMethod("addNoOpLogger", String::class.java)
		.apply {
			isAccessible = true
		}
	addNoOpLogger(loggerFactory, "StartParameterUtils")
} else {
	error("AGP version changed, review hack.")
}
