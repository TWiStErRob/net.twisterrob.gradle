package net.twisterrob.gradle.android

import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider

interface ManifestFiles {
	val all: Provider<out List<RegularFile>>
}
