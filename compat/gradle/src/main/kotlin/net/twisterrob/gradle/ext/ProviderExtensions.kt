package net.twisterrob.gradle.ext

import org.gradle.api.provider.Provider
import org.gradle.util.GradleVersion

fun <T : Any, U : Any, V : Any, R : Any> Provider<T>.zip(
	other1: Provider<U>,
	other2: Provider<V>,
	combiner: (value1: T, value2: U, value3: V) -> R
): Provider<R> =
	this.zip(other1.zip(other2, ::Pair)) { value1, value2Andvalue3 ->
		val (value2, value3) = value2Andvalue3
		combiner(value1, value2, value3)
	}

/**
 * Compatibility function that takes into account the version of Gradle to call [Provider.forUseAtConfigurationTime].
 *
 *  * [Added in Gradle 6.5](https://github.com/gradle/gradle/commit/934c0f0b610066179389ba1189950433db5ed85f)
 *  * [Marked for replacement in Gradle 7.4](https://github.com/gradle/gradle/pull/19732)
 *  * [Deprecated in Gradle 7.3 RC4](https://github.com/gradle/gradle/commit/e9ebbf68d212edc8d082d77af65e3bbeeb553bd0)
 *  * [Nagging in Gradle 7.4](https://github.com/gradle/gradle/commit/e7fb539141c19807c7d57cbe18a89f67d94a9a49)
 *  * [Deprecated in Gradle 7.4](https://docs.gradle.org/8.5/userguide/upgrading_version_7.html#for_use_at_configuration_time_deprecation)
 *  * [Removed in Gradle 9.0?](???)
 *
 * @see Provider.forUseAtConfigurationTime
 */
@Suppress("detekt.FunctionMaxLength") // Gradle's function name.
fun <T : Any> Provider<T>.forUseAtConfigurationTimeCompat(): Provider<T> =
	if (GradleVersion.current() < GradleVersion.version("6.5")) {
		// Gradle < 6.5 doesn't have this function.
		this
	} else if (GradleVersion.current() < GradleVersion.version("7.4")) {
		// Gradle 6.5 - 7.3 requires this function to be called.
		@Suppress("DEPRECATION")
		this.forUseAtConfigurationTime()
	} else {
		// Gradle >= 7.4 deprecated this function in favor of not calling it (became no-op, and will eventually nag).
		this
	}

/**
 * Polyfill for deprecated method that was removed in Gradle 9.0.
 * @deprecated remove call or use [forUseAtConfigurationTimeCompat].
 */
@Deprecated(message = "Gradle 9.0 removed this method, remove call or use a Gradle-version-agnostic forUseAtConfigurationTimeCompat() instead.")
private fun <T : Any> Provider<T>.forUseAtConfigurationTime(): Provider<T> =
	Provider::class.java.getDeclaredMethod("forUseAtConfigurationTime")
		.invoke(this)
		.let { @Suppress("UNCHECKED_CAST") (it as Provider<T>) }
