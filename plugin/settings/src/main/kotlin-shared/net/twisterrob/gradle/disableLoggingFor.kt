@file:JvmMultifileClass
@file:JvmName("GradleUtils")

package net.twisterrob.gradle

import org.slf4j.ILoggerFactory
import org.slf4j.LoggerFactory
import java.lang.reflect.Method

/**
 * Disable logging for a specific class. Useful for disabling noisy plugins.
 * Source: https://issuetracker.google.com/issues/247906487#comment10
 *
 * **WARNING**: Must be called early in the build before the loggers are created.
 * If a class caches the logger (likely), they won't re-query it from the factory.
 * Author recommends putting the calls in root build.gradle.
 *
 * Examples:
 * * Gradle's configuration cache is really noisy, when nothing really happened (happy path).
 *    Even when `org.gradle.unsafe.configuration-cache.quiet=true`.
 *    Source: `org.gradle.configurationcache.problems.ConfigurationCacheProblems`.
 *    ```text
 *    0 problems were found storing the configuration cache.
 *
 *    See the complete report at file:///.../build/reports/configuration-cache/<hashes>/configuration-cache-report.html
 *    ```
 *    Note: it is recommended to set `org.gradle.unsafe.configuration-cache-problems=fail`,
 *    so if there are problems you get an exception, because the warnings will be silenced too.
 * * `com.gradle.plugin-publish`: on every configured project where `org.gradle.signing` is also applied.
 *    Source: `com.gradle.publish.PublishTask`.
 *    ```text
 *    > Configure project :module
 *    Signing plugin detected. Will automatically sign the published artifacts.
 *    ```
 * * AGP 7.4:
 *    Source: `com.android.build.api.component.impl.MutableListBackedUpWithListProperty`
 *    Source: `com.android.build.api.component.impl.MutableMapBackedUpWithMapProperty`
 *    ```text
 *    > Task :kaptDebugKotlin
 *    Values of variant API AnnotationProcessorOptions.arguments are queried and may return non final values, this is unsupported
 *    ```
 * * `org.jetbrains.dokka`: on every `dokkaJavadoc` task execution, it outputs about 10 lines of "progress".
 *    Source: `org.jetbrains.dokka.gradle.AbstractDokkaTask`.
 *    Sadly it's logging to `org.gradle.api.Task` logger, so it's infeasible to silence
 *    See [Dokka issue](https://github.com/Kotlin/dokka/issues/1894#issuecomment-1378116917) for more details.
 */
fun disableLoggingFor(name: String) {
	val loggerFactory: ILoggerFactory = LoggerFactory.getILoggerFactory()
	val addNoOpLogger: Method = loggerFactory::class.java
		.getDeclaredMethod("addNoOpLogger", String::class.java)
		.apply { isAccessible = true }
	addNoOpLogger.invoke(loggerFactory, name)
}
