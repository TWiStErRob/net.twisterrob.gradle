package com.android.build.gradle.internal.transforms

import proguard.Configuration

/**
 * Expose configuration publicly to be able to adjust it from this plugin.
 * @see ProGuardTransform.doMinification for what AGP sets up
 * @see BaseProguardAction.setActions for what AGP sets up
 */
internal val BaseProguardAction.configuration: Configuration get() = this.configuration
