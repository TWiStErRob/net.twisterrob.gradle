package net.twisterrob.gradle.internal.android

import com.android.build.api.dsl.Lint
import org.gradle.api.Incubating

@get:Incubating
@set:Incubating
@Suppress("detekt.BooleanPropertyNaming") // Following AGP's naming.
var Lint.checkAllWarningsCompat70x: Boolean by Lint::isCheckAllWarnings
