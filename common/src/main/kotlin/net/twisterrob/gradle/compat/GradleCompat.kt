@file:Suppress("UnstableApiUsage")

package net.twisterrob.gradle.compat

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.util.GradleVersion
import java.io.File

/**
 * @param S `Any` restriction is arbitrary,
 * because otherwise [`.map {}` gives a warning](https://youtrack.jetbrains.com/issue/KT-36770).
 */
fun <T, S : Any> Provider<T>.flatMapCompat(transformer: (T) -> Provider<S>): Provider<S> =
	when {
		GradleVersion.current().baseVersion < GradleVersion.version("5.0") -> {
			this.map { transformer(it).get() }
		}
		else -> {
			// New in Gradle 5.0.
			this.flatMap(transformer)
		}
	}

/**
 * Gradle 4.3-6.9 compatible version of [ObjectFactory.fileProperty].
 *
 * @see RegularFileProperty.set
 * @see RegularFileProperty.convention
 */
fun RegularFileProperty.conventionCompat(file: Provider<RegularFile>): RegularFileProperty =
	when {
		GradleVersion.current().baseVersion < GradleVersion.version("5.1") -> {
			if (this.isPresent) {
				error("A value has already been specified for $this, cannot polyfill convention behavior.")
			}
			this.set(file)
			this
		}
		else -> {
			// New in Gradle 5.1, https://docs.gradle.org/5.1/release-notes.html#specify-a-convention-for-a-property.
			this.convention(file)
		}
	}

/**
 * Gradle 4.3-6.9 compatible version of [ObjectFactory.fileProperty].
 * @param task is necessary to because historically this was [DefaultTask.newInputFile] or [DefaultTask.newOutputFile].
 *
 * @see DefaultTask.newInputFile
 * @see DefaultTask.newOutputFile
 * @see ObjectFactory.fileProperty
 */
fun ObjectFactory.filePropertyCompat(task: DefaultTask, isInput: Boolean): RegularFileProperty =
	when {
		GradleVersion.current().baseVersion < GradleVersion.version("5.0") -> {
			if (isInput) {
				@Suppress("DEPRECATION")
				task.newInputFile()
			} else {
				@Suppress("DEPRECATION")
				task.newOutputFile()
			}
		}
		else -> {
			// New in Gradle 5.0.
			this.fileProperty()
		}
	}

/**
 * Gradle 4.3-6.9 compatible version of [ObjectFactory.fileProperty].
 * The reason for being on [DefaultTask] is historical existence of [DefaultTask.newInputFile].
 *
 * @see DefaultTask.newInputFile
 * @see ObjectFactory.fileProperty
 */
fun DefaultTask.newInputFileCompat(): RegularFileProperty =
	when {
		GradleVersion.current().baseVersion < GradleVersion.version("5.0") -> {
			@Suppress("DEPRECATION")
			this.newInputFile()
		}
		else -> {
			// New in Gradle 5.0.
			this.project.objects.fileProperty()
		}
	}

/**
 * Gradle 4.3-6.9 compatible version of [ObjectFactory.fileProperty].
 * The reason for being on [DefaultTask] is historical existence of [DefaultTask.newOutputFile].
 *
 * @see DefaultTask.newOutputFile
 * @see ObjectFactory.fileProperty
 */
fun DefaultTask.newOutputFileCompat(): RegularFileProperty =
	when {
		GradleVersion.current().baseVersion < GradleVersion.version("5.0") -> {
			@Suppress("DEPRECATION")
			this.newOutputFile()
		}
		else -> {
			// New in Gradle 5.0.
			this.project.objects.fileProperty()
		}
	}

/**
 * Gradle 4.3-6.9 compatible version of [ProjectLayout.dir].
 * @param project is necessary, because shims need access to services on [Project].
 *
 * @see ProjectLayout.directoryProperty
 * @see ObjectFactory.directoryProperty
 */
fun ProjectLayout.dirCompat(project: Project, provider: Provider<File>): Provider<Directory> =
	when {
		GradleVersion.current().baseVersion < GradleVersion.version("5.0") -> {
			project.providers.provider {
				// Create a DirectoryProperty only because there's no other way to create a Directory.
				@Suppress("DEPRECATION")
				this.directoryProperty().apply { set(provider.get()) }.get()
			}
		}
		GradleVersion.current().baseVersion < GradleVersion.version("6.0") -> {
			// Note: flatMap and directoryProperty is new in Gradle 5.0.
			this.file(provider).flatMap {
				// Create a layout.file() first to have a Provider that lazily maps the value.
				project.objects.directoryProperty().apply { set(it.asFile) }
			}
		}
		else -> {
			// New in Gradle 6.0.
			this.dir(provider)
		}
	}

/**
 * Polyfill as reflective call, as this method was...
 *  * [Added in Gradle 4.3](https://docs.gradle.org/4.3/release-notes.html#improvements-for-plugin-authors)
 *  * [Deprecated in Gradle 5.0](https://docs.gradle.org/5.0/release-notes.html#changes-to-incubating-factory-methods-for-creating-properties)
 *  * [Removed in Gradle 6.0](https://docs.gradle.org/6.0/userguide/upgrading_version_5.html#replaced_and_removed_apis)
 *
 * @see DefaultTask.newInputFileCompat
 */
@Deprecated(
	message = "Replaced with ObjectFactory.fileProperty()",
	replaceWith = ReplaceWith("project.objects.fileProperty()")
)
fun DefaultTask.newInputFile(): RegularFileProperty {
	val newInputFile = DefaultTask::class.java.getDeclaredMethod("newInputFile").apply {
		// protected to public as this extension function is static and external to DefaultTask.
		isAccessible = true
	}
	return newInputFile(this) as RegularFileProperty
}

/**
 * Polyfill as reflective call, as this method was...
 *  * [Added in Gradle 4.3](https://docs.gradle.org/4.3/release-notes.html#improvements-for-plugin-authors)
 *  * [Deprecated in Gradle 5.0](https://docs.gradle.org/5.0/release-notes.html#changes-to-incubating-factory-methods-for-creating-properties)
 *  * [Removed in Gradle 6.0](https://docs.gradle.org/6.0/userguide/upgrading_version_5.html#replaced_and_removed_apis)
 *
 * @see DefaultTask.newOutputFileCompat
 */
@Deprecated(
	message = "Replaced with ObjectFactory.fileProperty()",
	replaceWith = ReplaceWith("project.objects.fileProperty()")
)
fun DefaultTask.newOutputFile(): RegularFileProperty {
	val newOutputFile = DefaultTask::class.java.getDeclaredMethod("newOutputFile").apply {
		// protected to public as this extension function is static and external to DefaultTask.
		isAccessible = true
	}
	return newOutputFile(this) as RegularFileProperty
}

/**
 * Polyfill as reflective call, as this method was...
 *  * [Added in Gradle 4.3](https://docs.gradle.org/4.3/release-notes.html#improvements-for-plugin-authors)
 *  * [Deprecated in Gradle 5.0](https://docs.gradle.org/5.0/release-notes.html#changes-to-incubating-factory-methods-for-creating-properties)
 *  * [Removed in Gradle 6.0](https://docs.gradle.org/current/userguide/upgrading_version_5.html#methods_on_defaulttask_and_projectlayout_replaced_with_objectfactory)
 *
 * @see ObjectFactory.directoryProperty
 */
@Deprecated(
	message = "Replaced with ObjectFactory.directoryProperty()",
	replaceWith = ReplaceWith("objects.directoryProperty()")
)
fun ProjectLayout.directoryProperty(): DirectoryProperty {
	val directoryProperty = ProjectLayout::class.java.getDeclaredMethod("directoryProperty")
	return directoryProperty(this) as DirectoryProperty
}
