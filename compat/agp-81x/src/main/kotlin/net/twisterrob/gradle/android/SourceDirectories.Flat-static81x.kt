package net.twisterrob.gradle.android

import com.android.build.api.variant.SourceDirectories
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider

val SourceDirectories.Flat.static81x: Provider<out Collection<Directory>>
	get() = all.map { directories ->
		directories.filter { "generated" !in it.asFile.path }
	}
