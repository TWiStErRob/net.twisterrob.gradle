package net.twisterrob.gradle.internal.android

import com.android.build.gradle.internal.dsl.PackagingOptions
import net.twisterrob.gradle.android.ResourcesPackagingOptionsCompat

val PackagingOptions.resourcesCompat80x: ResourcesPackagingOptionsCompat
	get() = object : ResourcesPackagingOptionsCompat {
		override val excludes: MutableSet<String>
			get() = resources.excludes

		override val pickFirsts: MutableSet<String>
			get() = resources.pickFirsts

		override val merges: MutableSet<String>
			get() = resources.merges
	}
