plugins {
	id("com.android.application") version "7.4.0"
	// TODEL Duplicate function hack from below, use plugin 0.15 variant.
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

if (com.android.Version.ANDROID_GRADLE_PLUGIN_VERSION.startsWith("7.")) {
	// Copy of plugin\settings\src\main\kotlin\net\twisterrob\gradle\doNotNagAbout.kt temporarily until 0.15 is released.
	fun doNotNagAbout(message: String) {
		val logger: Any = org.gradle.internal.deprecation.DeprecationLogger::class.java
			.getDeclaredField("DEPRECATED_FEATURE_HANDLER")
			.apply { isAccessible = true }
			.get(null)

		@Suppress("UNCHECKED_CAST")
		val messages: MutableSet<String> =
			org.gradle.internal.featurelifecycle.LoggingDeprecatedFeatureHandler::class.java
				.getDeclaredField("messages")
				.apply { isAccessible = true }
				.get(logger) as MutableSet<String>

		messages.add(message)
	}

	val gradleVersion: String = GradleVersion.current().baseVersion.version

// See https://issuetracker.google.com/issues/264177800
	@Suppress("MaxLineLength")
	doNotNagAbout(
		"The Report.destination property has been deprecated. " +
				"This is scheduled to be removed in Gradle 9.0. " +
				"Please use the outputLocation property instead. " +
				"See https://docs.gradle.org/${gradleVersion}/dsl/org.gradle.api.reporting.Report.html#org.gradle.api.reporting.Report:destination for more details."
	)
} else {
	error("AGP major version changed, review hack.")
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
