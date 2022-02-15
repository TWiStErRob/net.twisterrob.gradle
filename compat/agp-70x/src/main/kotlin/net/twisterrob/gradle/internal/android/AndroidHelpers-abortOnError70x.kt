package net.twisterrob.gradle.internal.android

import com.android.build.api.dsl.Lint
import org.gradle.api.Incubating

/**
 * 7.1.x renamed [Lint.isAbortOnError] to `abortOnError` so we need to compile against 7.0.x.
 */
@get:Incubating
@set:Incubating
var Lint.abortOnError70x: Boolean
	get() = isAbortOnError
	set(value) {
		isAbortOnError = value
	}
