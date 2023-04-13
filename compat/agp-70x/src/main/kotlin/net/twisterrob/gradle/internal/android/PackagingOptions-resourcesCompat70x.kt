package net.twisterrob.gradle.internal.android

import com.android.build.gradle.internal.dsl.PackagingOptions
import net.twisterrob.gradle.android.ResourcesPackagingOptionsCompat
import org.gradle.api.Incubating

val PackagingOptions.resourcesCompat70x: ResourcesPackagingOptionsCompat
	@Incubating
	get() = object : ResourcesPackagingOptionsCompat {
		override val excludes: MutableSet<String>
			get() = resources.excludes

		override val pickFirsts: MutableSet<String>
			get() = resources.pickFirsts

		override val merges: MutableSet<String>
			get() = resources.merges
	}
