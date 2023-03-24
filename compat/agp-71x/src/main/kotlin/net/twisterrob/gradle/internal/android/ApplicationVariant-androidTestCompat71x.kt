package net.twisterrob.gradle.internal.android

import com.android.build.api.variant.AndroidTest
import com.android.build.api.variant.ApplicationVariant
import com.android.build.api.variant.HasAndroidTest

/**
 * Introduced in 7.0.x as [ApplicationVariant.androidTest],
 * Moved in 7.1.x to [com.android.build.api.variant.HasAndroidTest.androidTest].
 * Which means compiling against 7.1+ will call the wrong bytecode instruction and [NoSuchMethodError] on 7.0.
 */
val ApplicationVariant.androidTestCompat71x: AndroidTest? by HasAndroidTest::androidTest
