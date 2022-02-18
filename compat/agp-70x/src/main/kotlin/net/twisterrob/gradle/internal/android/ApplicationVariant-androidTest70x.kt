package net.twisterrob.gradle.internal.android

import com.android.build.api.variant.AndroidTest
import com.android.build.api.variant.ApplicationVariant

val ApplicationVariant.androidTest70x: AndroidTest?
	get() = this.androidTest
