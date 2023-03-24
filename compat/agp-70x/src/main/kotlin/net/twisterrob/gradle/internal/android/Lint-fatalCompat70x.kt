package net.twisterrob.gradle.internal.android

import com.android.build.api.dsl.Lint
import org.gradle.api.Incubating

@Incubating
fun Lint.fatalCompat70x(id: String) {
	this.fatal(id)
}
