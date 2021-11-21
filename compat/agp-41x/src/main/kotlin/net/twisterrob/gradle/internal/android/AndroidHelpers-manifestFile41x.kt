package net.twisterrob.gradle.internal.android

import com.android.build.gradle.tasks.ManifestProcessorTask
import com.android.build.gradle.tasks.ProcessApplicationManifest
import com.android.build.gradle.tasks.ProcessMultiApkApplicationManifest
import org.gradle.api.provider.Provider
import java.io.File

val ManifestProcessorTask.manifestFile41x: Provider<File>
	get() =
		when (this) {
			is ProcessApplicationManifest -> mergedManifest.asFile
			is ProcessMultiApkApplicationManifest -> mainMergedManifest.asFile
			else -> error("$this is an unsupported ${ManifestProcessorTask::class}")
		}
