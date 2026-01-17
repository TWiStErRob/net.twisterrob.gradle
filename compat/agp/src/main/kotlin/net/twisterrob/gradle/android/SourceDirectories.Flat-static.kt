package net.twisterrob.gradle.android

import com.android.build.api.variant.SourceDirectories
import net.twisterrob.gradle.common.AGPVersions
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider

val SourceDirectories.Flat.staticCompat: Provider<out Collection<Directory>>
	get() = when {
		AGPVersions.v84x <= AGPVersions.CLASSPATH -> this.static
		AGPVersions.v81x <= AGPVersions.CLASSPATH -> this.static81x
		else -> AGPVersions.olderThan81NotSupported(AGPVersions.CLASSPATH)
	}
