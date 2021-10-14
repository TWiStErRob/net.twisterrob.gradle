@file:JvmMultifileClass
@file:JvmName("AndroidHelpers")

package net.twisterrob.gradle.android

import com.android.build.gradle.internal.dsl.DefaultConfig
import com.android.builder.model.ApiVersion
import net.twisterrob.gradle.common.ANDROID_GRADLE_PLUGIN_VERSION

var DefaultConfig.minSdkVersionCompat: ApiVersion?
	get() = this.minSdkVersion
	set(version) {
		when {
			ANDROID_GRADLE_PLUGIN_VERSION >= "4.1.0" -> {
				this.minSdkVersion = version
			}
			ANDROID_GRADLE_PLUGIN_VERSION >= "4.0.0" -> {
				// Need to explicitly call as signature changed. Before 4.1 it was a builder, after it's void.
				Class.forName("com.android.builder.core.AbstractProductFlavor")
					.getDeclaredMethod("setMinSdkVersion", ApiVersion::class.java)
					.invoke(this, version)
			}
			else -> TODO("AGP 3.x not supported")
		}
	}


var DefaultConfig.targetSdkVersionCompat: ApiVersion?
	get() = this.targetSdkVersion
	set(version) {
		when {
			ANDROID_GRADLE_PLUGIN_VERSION >= "4.1.0" -> {
				this.targetSdkVersion = version
			}
			ANDROID_GRADLE_PLUGIN_VERSION >= "4.0.0" -> {
				// Need to explicitly call as signature changed. Before 4.1 it was a builder, after it's void.
				Class.forName("com.android.builder.core.AbstractProductFlavor")
					.getDeclaredMethod("setTargetSdkVersion", ApiVersion::class.java)
					.invoke(this, version)
			}
			else -> TODO("AGP 3.x not supported")
		}
	}
