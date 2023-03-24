package net.twisterrob.gradle.internal.android

import com.android.build.api.variant.ApplicationVariant
import com.android.build.api.variant.CanMinifyCode
import org.gradle.api.Incubating

@get:Incubating
val ApplicationVariant.isMinifyEnabledCompat74x: Boolean by CanMinifyCode::isMinifyEnabled
