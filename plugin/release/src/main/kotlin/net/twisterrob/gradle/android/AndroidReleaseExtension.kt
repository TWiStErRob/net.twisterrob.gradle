package net.twisterrob.gradle.android

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.file.DirectoryProperty
import org.gradle.kotlin.dsl.getByName

@Suppress("detekt.UnnecessaryAbstractClass") // Gradle convention.
abstract class AndroidReleaseExtension {

	abstract val directory: DirectoryProperty

	companion object {

		internal const val NAME: String = "release"

		fun from(android: CommonExtension): AndroidReleaseExtension =
			android.extensionsCompat.getByName<AndroidReleaseExtension>(NAME)
	}
}
