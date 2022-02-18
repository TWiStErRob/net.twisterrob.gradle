@file:JvmName("Versions")

package net.twisterrob.gradle.android

/**
 * Keep it in sync with AppCompat's minimum.
 */
const val VERSION_SDK_MINIMUM: Int = 14

/**
 * Latest SDK version available, Google Play Store has stringent rules, so keep up to date.
 */
const val VERSION_SDK_TARGET: Int = 30

/**
 * Latest SDK version available, useful for discovering deprecated methods and getting new features like `.findViewById<T>()`.
 */
const val VERSION_SDK_COMPILE: Int = 30

/**
 * Note: format changed at 9 Pie, was 8.1.0 Oreo.
 */
const val VERSION_SDK_COMPILE_NAME: String = "11" // Android 11 (R)

/**
 * Latest build tools version available, there's no reason to hold back.
 */
const val VERSION_BUILD_TOOLS: String = "32.0.0"
