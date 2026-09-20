package net.twisterrob.gradle.android

import com.android.build.api.variant.Sources
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider

val Sources.manifestsCompat: Provider<out List<RegularFile>>
	get() = this.manifests.all
