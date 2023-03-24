package net.twisterrob.gradle.internal.android

import com.android.build.api.dsl.Lint
import org.gradle.api.Incubating

@get:Incubating
@set:Incubating
var Lint.checkAllWarningsCompat71x: Boolean by Lint::checkAllWarnings 
