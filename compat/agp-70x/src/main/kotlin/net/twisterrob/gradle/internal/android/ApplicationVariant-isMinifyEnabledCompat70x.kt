package net.twisterrob.gradle.internal.android

import com.android.build.api.variant.ApplicationVariant
import com.android.build.gradle.internal.component.ConsumableCreationConfig

val ApplicationVariant.isMinifyEnabledCompat70x: Boolean
	get() = (this as ConsumableCreationConfig).minifiedEnabled
