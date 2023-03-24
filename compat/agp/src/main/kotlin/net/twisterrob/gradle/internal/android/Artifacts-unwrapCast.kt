package net.twisterrob.gradle.internal.android

import com.android.build.api.artifact.Artifacts
import com.android.build.api.component.analytics.AnalyticsEnabledArtifacts

/**
 * Works for [AnalyticsEnabledArtifacts] (last checked AGP 7.4.2).
 *
 * @see com.android.build.api.component.analytics.AnalyticsEnabledArtifacts
 */
@Suppress("UNCHECKED_CAST")
fun <T : Artifacts> Artifacts.unwrapCast(): T =
	@Suppress("UseIfInsteadOfWhen") // Preferred for instanceof type checks.
	when (this) {
		is AnalyticsEnabledArtifacts -> this.delegate.unwrapCast()
		else -> this as T
	}
