package net.twisterrob.gradle

import org.slf4j.ILoggerFactory
import org.slf4j.LoggerFactory
import java.lang.reflect.Method

/**
 * Disable logging for a specific class. Useful for disabling noisy plugins.
 * Source: https://issuetracker.google.com/issues/247906487#comment10
 *
 * Examples:
 * * `com.gradle.plugin-publish`: on every configured project where `org.gradle.signing` is also applied.
 *   Source: `com.gradle.publish.PublishTask`.
 *   ```
 *   > Configure project :module
 *   Signing plugin detected. Will automatically sign the published artifacts.
 *   ```
 * * `org.jetbrains.dokka`: on every `dokkaJavadoc` task execution, it outputs about 10 lines of "progress".
 *   Source: `org.jetbrains.dokka.gradle.AbstractDokkaTask`.
 *   Sadly it's logging to `org.gradle.api.Task` logger, so it's inconvenient to silence
 *   See https://github.com/Kotlin/dokka/issues/1894#issuecomment-1378116917 for more details.
 */
fun disableLoggingFor(name: String) {
	val loggerFactory: ILoggerFactory = LoggerFactory.getILoggerFactory()
	val addNoOpLogger: Method = loggerFactory::class.java
		.getDeclaredMethod("addNoOpLogger", String::class.java)
		.apply {
			isAccessible = true
		}
	addNoOpLogger.invoke(loggerFactory, name)
}
