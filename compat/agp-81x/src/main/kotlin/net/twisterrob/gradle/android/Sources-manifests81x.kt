package net.twisterrob.gradle.android

import com.android.build.api.variant.InternalSources
import com.android.build.api.variant.Sources
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider

val Sources.manifests81x: ManifestFiles
	get() = object : ManifestFiles {
		override val all: Provider<out List<RegularFile>>
			get() {
				this@manifests81x as InternalSources
				return manifestFile.zip(manifestOverlayFiles) { a, b ->
					(listOf(a) + b).map { RegularFile { it } }
				}
			}
	}
