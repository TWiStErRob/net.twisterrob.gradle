package net.twisterrob.gradle.test

import org.gradle.testkit.runner.BuildResult
import org.jetbrains.annotations.Nullable

import javax.annotation.Nonnull

class BuildResultExtensions {

	static @Nullable String failReason(final @Nonnull BuildResult self) {
		return failureBlock('What went wrong', self)
	}

	static @Nullable String failSuggestion(final @Nonnull BuildResult self) {
		return failureBlock('Try', self)
	}

	static @Nullable String fullException(final @Nonnull BuildResult self) {
		return failureBlock('Exception is', self)
	}

	private static String failureBlock(String label, BuildResult self) {
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
