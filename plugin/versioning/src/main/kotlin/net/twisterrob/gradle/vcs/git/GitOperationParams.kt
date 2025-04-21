package net.twisterrob.gradle.vcs.git

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ValueSourceParameters
import java.io.File

internal interface GitOperationParams : ValueSourceParameters {
	val gitDir: DirectoryProperty
}

internal val GitOperationParams.gitDirFile: File
	get() = gitDir.get().asFile
