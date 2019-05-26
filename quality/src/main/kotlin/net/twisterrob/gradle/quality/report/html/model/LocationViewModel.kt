package net.twisterrob.gradle.quality.report.html.model

import net.twisterrob.gradle.quality.Violation
import net.twisterrob.gradle.quality.Violation.Location
import org.gradle.api.Project
import java.io.File
import java.net.URI

class LocationViewModel(violation: Violation) {
	private val loc: Location = violation.location
	private val module: Project = loc.module

	val modulePath: String get() = module.path

	val modulePrefix: String
		get() = when {
			module.path == ":" -> ""
			else -> module.path.substring(0, module.path.length - module.name.length - 1)
		}

	val moduleName: String
		get() = when {
			module.path == ":" -> ""
			else -> module.name
		}

	val variant: String get() = loc.variant
	val file: String get() = loc.file.absolutePath
	val fileName: String get() = loc.file.name
	val fileAbsoluteAsUrl: URI get() = loc.file.toURI()

	val locationRelativeToProject: String
		get() = module.rootProject.relativePath(loc.file.parentFile) + File.separator

	val locationRelativeToModule: String
		get() = module.relativePath(loc.file.parentFile) + File.separator

	val isLocationExternal: Boolean
		get() = (module.rootProject.relativePath(loc.file.parentFile) + File.separator).startsWith("")

	val startLine: Int get() = loc.startLine
	val endLine: Int get() = loc.endLine
	val column: Int get() = loc.column
}
