package net.twisterrob.gradle.android

import net.twisterrob.gradle.kotlin.dsl.extensions
import org.gradle.api.file.DirectoryProperty
import org.gradle.kotlin.dsl.getByName

@Suppress("detekt.UnnecessaryAbstractClass") // Gradle convention.
abstract class AndroidReleaseExtension {

	abstract val directory: DirectoryProperty

	companion object {

		internal const val NAME: String = "release"

		fun from(
			@Suppress("DEPRECATION" /* AGP 9.0 */)
			android: com.android.build.gradle.BaseExtension
		): AndroidReleaseExtension =
			android.extensions.getByName<AndroidReleaseExtension>(NAME)
	}
}
