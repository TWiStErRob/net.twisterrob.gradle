package net.twisterrob.gradle.android

import com.android.build.api.variant.Sources
import net.twisterrob.gradle.common.AGPVersions
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider

val Sources.manifestsCompat: Provider<out List<RegularFile>>
	get() = when {
		AGPVersions.v83x <= AGPVersions.CLASSPATH -> this.manifests.all
		AGPVersions.v81x <= AGPVersions.CLASSPATH -> this.manifests81x.all
		else -> AGPVersions.olderThan81NotSupported(AGPVersions.CLASSPATH)
	}
