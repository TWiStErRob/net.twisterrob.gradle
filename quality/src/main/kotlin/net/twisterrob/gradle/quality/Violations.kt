package net.twisterrob.gradle.quality

import com.android.build.gradle.external.cmake.server.Project
import java.io.File

class Violations(
	@JvmField val parser: String,
	@JvmField val module: String,
	@JvmField val variant: String,
	/**
	 * Parseable result.
	 */
	@JvmField val result: File,
	/**
	 * Human-consumable report.
	 */
	@JvmField val report: File,
	/**
	 * Report file missing, or error during read.
	 */
	@JvmField val violations: List<Violation>?
) {

	override fun toString() = "${module}:${parser}@${variant} (${result}): ${violations}"
}

class Violation(
	val rule: String,
	val category: String?,
	val severity: Severity,
	val message: String,
	val specifics: Map<String, String> = emptyMap(),
	val location: Location,
	val source: Source
) {

	enum class Severity {
		INFO,
		WARNING,
		ERROR
	}

	class Location(
		val module: Project,
		val variant: String,
		val file: File,
		val startLine: Int,
		val endLine: Int,
		val column: Int
	)

	class Source(
		val parser: String,
		val gatherer: String,
		val reporter: String,
		val source: String,
		val report: File,
		val humanReport: File?
	)
}
