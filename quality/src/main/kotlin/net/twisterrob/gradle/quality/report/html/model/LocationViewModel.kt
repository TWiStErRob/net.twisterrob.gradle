package net.twisterrob.gradle.quality.report.html.model

import net.twisterrob.gradle.quality.Violation
import net.twisterrob.gradle.quality.Violation.Location
import org.gradle.api.Project
import java.io.File
import java.net.URI

@Suppress("UseDataClass") // TODEL https://github.com/detekt/detekt/issues/5339
class LocationViewModel(violation: Violation) {
	private val loc: Location = violation.location
	private val module: Project = loc.module

	/**
	 * The full path of the module.
	 * Can be reconstructed also as
	 * [modulePrefix] + `:` + [moduleName]
	 *
	 * Since [modulePrefix] and [moduleName] can be empty, this will always give the right result.
	 */
	val modulePath: String
		get() = module.path

	/**
	 * The prefix leading up to the name of the module, but not including the separator.
	 * Root project has no prefix.
	 *
	 * @see [modulePath]
	 */
	val modulePrefix: String
		get() =
			if (module.path == ":") {
				""
			} else {
				module.path.substring(0, module.path.length - module.name.length - 1)
			}

	/**
	 * The name of the module.
	 * Root project has no name.
	 *
	 * @see [modulePath]
	 */
	val moduleName: String
		get() =
			if (module.path == ":") {
				""
			} else {
				module.name
			}

	val variant: String
		get() = loc.variant

	val file: String
		get() = loc.file.absolutePath

	val fileName: String
		get() = loc.file.name

	val fileAbsoluteAsUrl: URI
		get() = loc.file.absoluteFile.toURI()

	/**
	 * The directory of the file, relative to the project root, so the absolute path is:
	 * `rootProject.projectDir` + [locationRelativeToProject] + [fileName]
	 */
	val locationRelativeToProject: String
		get() = module.rootProject.relativePath(loc.file.parentFile) + File.separator

	/**
	 * The directory of the file, relative to the module root, so the absolute path is:
	 * `module.projectDir` + [locationRelativeToModule] + [fileName]
	 */
	val locationRelativeToModule: String
		get() = module.relativePath(loc.file.parentFile) + File.separator

	/**
	 * Whether this location is outside the project root.
	 * Files are internal if they're located within the project / root module.
	 * This could be just one level up, or even a different drive on Windows.
	 * This would imply that it's better to use absolute paths in some places
	 * rather than [locationRelativeToProject] or [locationRelativeToModule].
	 * [locationRelativeToProject] or [locationRelativeToModule] will be absolute when this is true.
	 *
	 * Assumption: the root project physically contains all the other modules.
	 */
	val isLocationExternal: Boolean
		get() = (module.rootProject.relativePath(loc.file.parentFile) + File.separator).startsWith("")

	val startLine: Int
		get() = loc.startLine

	val endLine: Int
		get() = loc.endLine

	val column: Int
		get() = loc.column
}
