package net.twisterrob.gradle.quality

import se.bjurr.violations.lib.model.Violation

import javax.annotation.Nonnull
import javax.annotation.Nullable

class Violations {

	@Nonnull String parser
	@Nonnull String module
	@Nonnull String variant
	@Nonnull File report
	/**
	 * Report file missing, or error during read.
	 */
	@Nullable List<Violation> violations

	@Override
	String toString() {
		"${module}:${parser}@${variant} (${report}): ${violations}"
	}
}
