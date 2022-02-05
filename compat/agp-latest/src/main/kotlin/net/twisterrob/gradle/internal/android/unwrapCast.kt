package net.twisterrob.gradle.internal.android

import com.android.build.api.artifact.Artifacts
import com.android.build.api.component.analytics.AnalyticsEnabledArtifacts
import com.android.build.api.component.analytics.AnalyticsEnabledComponent
import com.android.build.api.variant.Component

/**
 * Works for [AnalyticsEnabledComponent] subclasses (last checked AGP 7.0.3):
 * @see com.android.build.api.component.analytics.AnalyticsEnabledTestFixtures
 *
 * @see com.android.build.api.component.analytics.AnalyticsEnabledVariant
 * @see com.android.build.api.component.analytics.AnalyticsEnabledApplicationVariant
 * @see com.android.build.api.component.analytics.AnalyticsEnabledLibraryVariant
 * @see com.android.build.api.component.analytics.AnalyticsEnabledTestVariant
 * @see com.android.build.api.component.analytics.AnalyticsEnabledDynamicFeatureVariant
 *
 * @see com.android.build.api.component.analytics.AnalyticsEnabledTestComponent
 * @see com.android.build.api.component.analytics.AnalyticsEnabledUnitTest
 * @see com.android.build.api.component.analytics.AnalyticsEnabledAndroidTest
 */
@Suppress("UNCHECKED_CAST")
fun <C : Component, T : C> C.unwrapCast(): T =
	when (this) {
		is AnalyticsEnabledComponent -> this.delegate.unwrapCast()
		else -> this
	} as T

@Suppress("UNCHECKED_CAST")
fun <T : Artifacts> Artifacts.unwrapCast(): T =
	when (this) {
		is AnalyticsEnabledArtifacts -> this.delegate.unwrapCast()
		else -> this
	} as T
