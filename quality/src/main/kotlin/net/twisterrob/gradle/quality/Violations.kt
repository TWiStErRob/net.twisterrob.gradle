package net.twisterrob.gradle.quality

import se.bjurr.violations.lib.model.Violation
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
