package net.twisterrob.gradle.internal.android

import com.android.build.api.variant.AndroidTest
import com.android.build.api.variant.ApplicationVariant

val ApplicationVariant.androidTestCompat70x: AndroidTest? by ApplicationVariant::androidTest
