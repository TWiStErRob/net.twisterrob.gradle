package net.twisterrob.gradle.webjars

import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.CopySpec
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.file.RelativePath
import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import javax.inject.Inject

abstract class ExtractWebJarAction @Inject constructor(
	private val files: FileSystemOperations,
	private val archives: ArchiveOperations,
) : WorkAction<ExtractWebJarAction.Parameters> {

	interface Parameters : WorkParameters {
		val localWebJar: RegularFileProperty
		val artifactId: Property<String>
		val outputDirectory: DirectoryProperty
	}

	override fun execute() {
		// Note: using copy, instead of sync, because there are multiple operations on the same output directory.
		files.copy {
			duplicatesStrategy = DuplicatesStrategy.FAIL
			into(parameters.outputDirectory)
			from(archives.zipTree(parameters.localWebJar))
			configureFiles(parameters.artifactId.get())
			eachFile {
				// https://docs.gradle.org/current/userguide/working_with_files.html#ex-unpacking-a-subset-of-a-zip-file
				relativePath = RelativePath(true, relativePath.segments.last())
			}
			includeEmptyDirs = false
		}
	}

	// Can't use CopySpec.with, because .copySpec() is not available in any injected API.
	private fun CopySpec.configureFiles(artifactCoordinate: String) {
		val (group, module, version) = artifactCoordinate.split(":")
		when (group) {
			"org.webjars" -> {
				val folder = "META-INF/resources/webjars/${module}/${version}"
				include("${folder}/*.js")
				include("${folder}/*.min.js")
				exclude("${folder}/webjars-requirejs.js")
			}
			"org.webjars.npm" -> {
				val folder = "META-INF/resources/webjars/${module}/${version}/dist"
				include("${folder}/${module}.js")
				include("${folder}/${module}.min.js")
				exclude("${folder}/package.js")
			}
			else -> {
				error("Unknown webjar group: ${group}")
			}
		}
	}
}
