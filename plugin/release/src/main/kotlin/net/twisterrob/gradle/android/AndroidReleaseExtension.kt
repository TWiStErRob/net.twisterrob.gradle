package net.twisterrob.gradle.android

import org.gradle.api.file.DirectoryProperty
import org.gradle.kotlin.dsl.getByName

@Suppress("detekt.UnnecessaryAbstractClass") // Gradle convention.
abstract class AndroidReleaseExtension {

	abstract val directory: DirectoryProperty

	companion object {

		internal const val NAME: String = "release"

		fun from(android: BaseExtension): AndroidReleaseExtension =
			android.extensions.getByName<AndroidReleaseExtension>(NAME)
	}
}
