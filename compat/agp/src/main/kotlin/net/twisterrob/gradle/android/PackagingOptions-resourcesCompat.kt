package net.twisterrob.gradle.android

import com.android.build.gradle.internal.dsl.PackagingOptions
import net.twisterrob.gradle.common.AGPVersions
import net.twisterrob.gradle.internal.android.resourcesCompat70x
import net.twisterrob.gradle.internal.android.resourcesCompat80x

/**
 * Compatibility version for [PackagingOptions.resources] DSL:
 * `android.packaging.resources { ... }`.
 *
 * * AGP 4.0.x had [com.android.build.gradle.internal.dsl.PackagingOptions].
 * * AGP 4.1.x implements [com.android.build.api.dsl.PackagingOptions] interface
 * * AGP 4.2.x implements [com.android.build.api.dsl.PackagingOptions] interface
 *             returning [com.android.build.api.dsl.ResourcesPackagingOptions].
 * * AGP 8.0.x renamed [com.android.build.api.dsl.PackagingOptions] to [com.android.build.api.dsl.Packaging].
 * * AGP 8.0.x renamed [com.android.build.api.dsl.ResourcesPackagingOptions] to [com.android.build.api.dsl.ResourcesPackaging].
 * * AGP 8.0.x implements [com.android.build.api.dsl.Packaging] interface
 *             returning [com.android.build.api.dsl.ResourcesPackaging].
 *
 * This causes AGP compiled with 8.x to fail on 7.x with [java.lang.NoSuchMethodError]:
 * > com.android.build.gradle.internal.dsl.PackagingOptions.getResources()Lcom/android/build/api/dsl/ResourcesPackaging;
 */
@Suppress("KDocUnresolvedReference")
val PackagingOptions.resourcesCompat: ResourcesPackagingOptionsCompat
	get() =
		@Suppress("MISSING_DEPENDENCY_SUPERCLASS") // Will exist when necessary.
		when {
			AGPVersions.v80x <= AGPVersions.CLASSPATH -> this.resourcesCompat80x
			AGPVersions.v70x <= AGPVersions.CLASSPATH -> this.resourcesCompat70x
			else -> AGPVersions.olderThan7NotSupported(AGPVersions.CLASSPATH)
		}

fun PackagingOptions.resourcesCompat(action: ResourcesPackagingOptionsCompat.() -> Unit) {
	this.resourcesCompat.action()
}
