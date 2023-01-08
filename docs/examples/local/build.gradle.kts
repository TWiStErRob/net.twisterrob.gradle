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

// TODEL https://issuetracker.google.com/issues/264177800
if (com.android.Version.ANDROID_GRADLE_PLUGIN_VERSION < "7.4.1") {
	// Copy of plugin\settings\src\main\kotlin\net\twisterrob\gradle\doNotNagAbout.kt temporarily until 0.15 is released.

	class IgnoringSet(
		private val backingSet: MutableSet<String>
	) : MutableSet<String> by backingSet {

		private val ignores: MutableSet<Regex> = mutableSetOf()

		fun ignorePattern(regex: Regex) {
			ignores.add(regex)
		}

		override fun add(element: String): Boolean {
			val isIgnored = ignores.any { it.matches(element) }
			val isNew = backingSet.add(element)
			return !isIgnored && isNew
		}
	}
	fun doNotNagAbout(message: String) {
		// "fail" was not a valid option for --warning-mode before Gradle 5.6.0.
		// In Gradle 4.7.0 (c633542) org.gradle.util.SingleMessageLogger#deprecatedFeatureHandler came to be in a refactor.
		// In Gradle 6.2.0 it was split (247fd32) to org.gradle.util.DeprecationLogger#deprecatedFeatureHandler
		// and then further split (308086a) to org.gradle.internal.deprecation.DeprecationLogger#deprecatedFeatureHandler
		// and then renamed (a75aedd) to #DEPRECATED_FEATURE_HANDLER.
		val loggerField =
			if (GradleVersion.version("6.2.0") <= GradleVersion.current().baseVersion) {
				Class.forName("org.gradle.internal.deprecation.DeprecationLogger")
					.getDeclaredField("DEPRECATED_FEATURE_HANDLER")
					.apply { isAccessible = true }
			} else if (GradleVersion.version("4.7.0") <= GradleVersion.current().baseVersion) {
				Class.forName("org.gradle.util.SingleMessageLogger")
					.getDeclaredField("deprecatedFeatureHandler")
					.apply { isAccessible = true }
			} else {
				error("Gradle ${GradleVersion.current()} too old, cannot ignore deprecation: $message")
			}
		val deprecationLogger: Any = loggerField.get(null)

		// LoggingDeprecatedFeatureHandler#messages was added in Gradle 1.8.
		val messagesField = org.gradle.internal.featurelifecycle.LoggingDeprecatedFeatureHandler::class.java
			.getDeclaredField("messages")
			.apply { isAccessible = true }
		@Suppress("UNCHECKED_CAST")
		val messages: MutableSet<String> = messagesField.get(deprecationLogger) as MutableSet<String>

		val ignore = if (messages is IgnoringSet) messages else IgnoringSet(messages)
		messagesField.set(deprecationLogger, ignore)
		// Ignoring with "startsWith" to disregard the stack trace. It's not ideal,
		// but it's the best we can do to counteract https://github.com/gradle/gradle/pull/22489 introduced in Gradle 8.0.
		ignore.ignorePattern(Regex("(?s)${Regex.escape(message)}.*"))
	}

	val gradleVersion: String = GradleVersion.current().version

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
