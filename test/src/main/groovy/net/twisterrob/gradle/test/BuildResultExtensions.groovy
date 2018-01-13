package net.twisterrob.gradle.test

import org.gradle.testkit.runner.BuildResult

import javax.annotation.Nonnull
import javax.annotation.Nullable
import javax.annotation.RegEx

class BuildResultExtensions {

	static void assertHasOutputLine(@Nonnull BuildResult self, @RegEx String expectedLineRegex) {
		assert self.output =~ /(?m)^${expectedLineRegex}$/
	}

	static @Nullable String failReason(@Nonnull BuildResult self) {
		return failureBlock('What went wrong', self)
	}

	static @Nullable String failSuggestion(@Nonnull BuildResult self) {
		return failureBlock('Try', self)
	}

	static @Nullable String fullException(@Nonnull BuildResult self) {
		return failureBlock('Exception is', self)
	}

	private static String failureBlock(@Nonnull String label, @Nonnull BuildResult self) {
		def fullLabel = "* ${label}:"
		return self.output
		           .split(System.lineSeparator())
		           .dropWhile {it != fullLabel}
		           .drop(1)
		           .takeWhile {!it.startsWith("* ")}
		           .join(System.lineSeparator())
		           .trim()
	}
}
