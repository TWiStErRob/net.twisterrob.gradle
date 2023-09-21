package net.twisterrob.gradle.android

import com.android.build.gradle.BaseExtension
import net.twisterrob.gradle.kotlin.dsl.extensions
import org.gradle.api.file.DirectoryProperty
import org.gradle.kotlin.dsl.getByName

abstract class AndroidReleaseExtension {

	abstract val directory: DirectoryProperty

	companion object {

		internal const val NAME: String = "release"

		fun from(android: BaseExtension): AndroidReleaseExtension =
			android.extensions.getByName<AndroidReleaseExtension>(NAME)
	}
}
