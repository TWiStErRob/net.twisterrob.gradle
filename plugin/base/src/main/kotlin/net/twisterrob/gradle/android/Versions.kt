@file:JvmName("Versions")

package net.twisterrob.gradle.android

/**
 * Keep it in sync with AndroidX / JetPack's minimum.
 */
const val VERSION_SDK_MINIMUM: Int = 21

/**
 * Latest SDK version available, Google Play Store has stringent rules, so keep up to date.
 */
const val VERSION_SDK_TARGET: Int = 35

/**
 * Latest SDK version available, useful for discovering deprecated methods and getting new features like `.findViewById<T>()`.
 *
 * @see com.android.sdklib.SdkVersionInfo.HIGHEST_SUPPORTED_API
 * @see com.android.SdkConstants.MAX_SUPPORTED_ANDROID_PLATFORM_VERSION
 * @see com.android.SdkConstants.CURRENT_BUILD_TOOLS_VERSION
 */
const val VERSION_SDK_COMPILE: Int = 35

/**
 * Note: format changed at 9 Pie, was 8.1.0 Oreo.
 *
 * @see https://apilevels.com/
 */
const val VERSION_SDK_COMPILE_NAME: String = "15" // Android 15 (V)
