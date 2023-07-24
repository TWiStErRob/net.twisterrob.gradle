package net.twisterrob.gradle.internal.nagging

import org.gradle.api.GradleException
import org.gradle.internal.featurelifecycle.LoggingDeprecatedFeatureHandler
import org.gradle.util.GradleVersion
import java.lang.reflect.Field
import java.util.concurrent.atomic.AtomicInteger

/**
 * Reflection wrapper to access Gradle's internal nagging mechanism.
 * Encapsulates reflective accesses in a typed interface.
 */
@Suppress("PropertyUsedBeforeDeclaration") // Ordering is from high level to low level. High level properties are lazy.
internal object GradleNaggingReflection {

	/**
	 * @since Gradle 4.7.0 because of [deprecatedFeatureHandlerField].
	 */
	private val deprecatedFeatureHandler: Any
		get() = deprecatedFeatureHandlerField.get(null)

	/**
	 * @since Gradle 4.7.0 because of [deprecatedFeatureHandler].
	 */
	@Suppress("DoubleMutabilityForCollection") // It's reflective access to a field, which is actually final 😅.
	var messages: MutableSet<String>
		@Suppress("UNCHECKED_CAST")
		get() = messagesField.get(deprecatedFeatureHandler) as MutableSet<String>
		set(value) {
			messagesField.set(deprecatedFeatureHandler, value)
		}

	/**
	 * @since Gradle 5.6.0 because of [errorField].
	 */
	var error: GradleException?
		@Suppress("CastToNullableType") // Know the type and know it's nullable.
		get() = errorField.get(deprecatedFeatureHandler) as GradleException?
		set(value) {
			errorField.set(deprecatedFeatureHandler, value)
		}

	val remainingStackTraces: AtomicInteger
		get() = remainingStackTracesField.get(problemStream) as AtomicInteger

	private val problemStream: Any
		get() = problemStreamField.get(deprecatedFeatureHandler)

	private val problemStreamField: Field =
		Class.forName("org.gradle.internal.featurelifecycle.LoggingDeprecatedFeatureHandler")
			.getDeclaredField("problemStream")
			.apply { isAccessible = true }

	private val remainingStackTracesField: Field =
		Class.forName("org.gradle.internal.problems.DefaultProblemDiagnosticsFactory\$DefaultProblemStream")
			.getDeclaredField("remainingStackTraces")
			.apply { isAccessible = true }

	/**
	 * History:
	 * * Gradle 4.7.0 added (c633542) `org.gradle.util.SingleMessageLogger#deprecatedFeatureHandler` in a refactor.
	 * * Gradle 6.2.0 split (247fd32) to `org.gradle.util.DeprecationLogger#deprecatedFeatureHandler`
	 *    * and then further split (308086a) to `org.gradle.internal.deprecation.DeprecationLogger#deprecatedFeatureHandler`
	 *    * and then renamed (a75aedd) to `#DEPRECATED_FEATURE_HANDLER`.
	 * * Gradle 8.3-rc-1: structure hasn't been changed since.
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
	 *  * Gradle 8.3-rc-1 renamed (949be46/#25156) to `#loggedMessages`.
	 *
	 * @since Gradle 1.8 because it's not worth supporting older versions than this, might be possible.
	 */
	private val messagesField: Field =
		@Suppress("UseIfInsteadOfWhen")
		when {
			GradleVersion.version("8.3") <= GradleVersion.current().baseVersion -> { // 8.3.0 would fail, because of RC1
				LoggingDeprecatedFeatureHandler::class.java
					.getDeclaredField("loggedMessages")
					.apply { isAccessible = true }
			}
			else -> {
				LoggingDeprecatedFeatureHandler::class.java
					.getDeclaredField("messages")
					.apply { isAccessible = true }
			}
		}

	/**
	 * History:
	 *  * Gradle 5.6.0-RC1 added (947ff89/#9850) the `#error` field to support `--warning-mode=fail`.
	 *  * Gradle 8.3-rc-1 (949be46/#25156) changed behavior to set the field unconditionally.<br/>
	 *    Previously the condition was whether the message has been previously logged.
	 *
	 * @since Gradle 5.6.0 because of support.
	 */
	private val errorField: Field =
		@Suppress("UseIfInsteadOfWhen")
		when {
			GradleVersion.version("5.6.0") <= GradleVersion.current().baseVersion -> {
				LoggingDeprecatedFeatureHandler::class.java
					.getDeclaredField("error")
					.apply { isAccessible = true }
			}
			else -> {
				error("Gradle ${GradleVersion.current()} too old, failing on deprecations is not supported.")
			}
		}
}
