package net.twisterrob.gradle.internal.android

import com.android.build.gradle.tasks.ManifestProcessorTask
import org.gradle.api.provider.Provider
import java.io.File

val ManifestProcessorTask.manifestFile40x: Provider<File>
	get() =
		manifestOutputDirectory
			.file("AndroidManifest.xml")
			.map { it.asFile }
