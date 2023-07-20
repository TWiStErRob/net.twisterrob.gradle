package net.twisterrob.gradle.internal.nagging

import org.gradle.internal.featurelifecycle.LoggingDeprecatedFeatureHandler
import org.gradle.util.GradleVersion
import java.lang.reflect.Field

/**
 * Reflection wrapper to access Gradle's internal nagging mechanism.
 * Encapsulates reflective accesses in a typed interface.
 */
internal object GradleNaggingReflection {

	/**
	 * @since Gradle 4.7.0 because of [deprecatedFeatureHandlerField].
	 */
	private val deprecatedFeatureHandler: Any
		get() = deprecatedFeatureHandlerField.get(null)

	/**
	 * @since Gradle 4.7.0 because of [deprecatedFeatureHandler].
	 */
	var messages: MutableSet<String>
		@Suppress("UNCHECKED_CAST")
		get() = messagesField.get(deprecatedFeatureHandler) as MutableSet<String>
		set(value) {
			messagesField.set(deprecatedFeatureHandler, value)
		}

	/**
	 * History:
	 * * Gradle 4.7.0 added (c633542) `org.gradle.util.SingleMessageLogger#deprecatedFeatureHandler` in a refactor.
	 * * Gradle 6.2.0 split (247fd32) to `org.gradle.util.DeprecationLogger#deprecatedFeatureHandler`
	 *    * and then further split (308086a) to `org.gradle.internal.deprecation.DeprecationLogger#deprecatedFeatureHandler`
	 *    * and then renamed (a75aedd) to `#DEPRECATED_FEATURE_HANDLER`.
	 *
	 * @since Gradle 4.7.0 because it's not worth supporting older versions than this, might be possible.
	 */
	private val deprecatedFeatureHandlerField: Field =
		when {
			GradleVersion.version("6.2.0") <= GradleVersion.current().baseVersion -> {
				Class.forName("org.gradle.internal.deprecation.DeprecationLogger")
					.getDeclaredField("DEPRECATED_FEATURE_HANDLER")
					.apply { isAccessible = true }
			}
			GradleVersion.version("4.7.0") <= GradleVersion.current().baseVersion -> {
				Class.forName("org.gradle.util.SingleMessageLogger")
					.getDeclaredField("deprecatedFeatureHandler")
					.apply { isAccessible = true }
			}
			else -> {
				error("Gradle ${GradleVersion.current()} too old, cannot ignore deprecation nagging.")
			}
		}

	/**
	 * History:
	 *  * Gradle 1.0 added (e890b9e) `DeprecationLogger`, then later versions added many `Set<String>` fields to it.
	 *  * Gradle 1.5 added (90079eb) `SingleMessageLogger` in a refactor of merging `DeprecationLogger`.
	 *  * Gradle 1.6 added (32a43a3) `SingleMessageLogger#MESSAGES` field in a refactor.
	 *  * Gradle 1.8 added (b4bae87) `LoggingDeprecatedFeatureHandler` with `#messages` field in a refactor.
	 *
	 * @since Gradle 1.8 because it's not worth supporting older versions than this, might be possible.
	 */
	private val messagesField: Field =
		LoggingDeprecatedFeatureHandler::class.java
			.getDeclaredField("messages")
			.apply { isAccessible = true }
}
