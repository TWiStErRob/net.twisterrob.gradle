@file:Suppress(
	"TooManyFunctions", // TODO might be worth splitting by receiver and use @JvmMultifileClass?
	"UseIfInsteadOfWhen", // Preparing for future new version ranges.
)

package net.twisterrob.gradle.compat

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.provider.Provider
import org.gradle.api.reporting.ConfigurableReport
import org.gradle.api.reporting.DirectoryReport
import org.gradle.api.reporting.Report
import org.gradle.api.reporting.SingleFileReport
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.getPluginByName
import org.gradle.util.GradleVersion
import java.io.File

/**
 * @param T the input type
 * @param S the output type, may be nullable; upon returning null the provider will have no value.
 * @param transformer The transformer to apply to values.
 */
fun <T, S> Provider<T>.flatMapCompat(transformer: (T) -> Provider<S>): Provider<S> =
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
 * Gradle 4.3-6.9 compatible version of [RegularFileProperty.fileProvider].
 *
 * @see RegularFileProperty.set
 * @see RegularFileProperty.fileProvider
 */
fun RegularFileProperty.fileProviderCompat(task: DefaultTask, file: Provider<File>): RegularFileProperty =
	when {
		GradleVersion.current().baseVersion < GradleVersion.version("6.0") -> {
			this.set(file.map { fileValue ->
				// Convoluted way to create a RegularFile object.
				val property = task.project.objects.filePropertyCompat(task, false)
				property.apply { set(fileValue) }.get()
			})
			this
		}
		else -> {
			// New in Gradle 6.0, https://docs.gradle.org/6.0/release-notes.html#new-convenience-methods-for-bridging-between-a-regularfileproperty-or-directoryproperty-and-a-file.
			this.fileProvider(file)
		}
	}

/**
 * Gradle 4.3-6.9 compatible version of [ObjectFactory.fileProperty].
 * @param task is necessary to because historically this was [DefaultTask.newInputFile] or [DefaultTask.newOutputFile].
 * @param isInput what type of property to create: true=[DefaultTask.newInputFile], false=[DefaultTask.newOutputFile].
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
 * @param provider location of the directory.
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
	message = "Replaced with ObjectFactory.fileProperty().",
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
	message = "Replaced with ObjectFactory.fileProperty().",
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
 * @see ObjectFactory.fileProperty
 */
@Deprecated(
	message = "Replaced with ObjectFactory.fileProperty().",
	replaceWith = ReplaceWith("project.objects.fileProperty()")
)
fun ProjectLayout.fileProperty(): RegularFileProperty {
	val fileProperty = ProjectLayout::class.java.getDeclaredMethod("fileProperty")
	return fileProperty(this) as RegularFileProperty
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
	message = "Replaced with ObjectFactory.directoryProperty().",
	replaceWith = ReplaceWith("project.objects.directoryProperty()")
)
fun ProjectLayout.directoryProperty(): DirectoryProperty {
	val directoryProperty = ProjectLayout::class.java.getDeclaredMethod("directoryProperty")
	return directoryProperty(this) as DirectoryProperty
}

/**
 * Gradle 4.3-7.3 compatible version of [BasePluginExtension.getArchivesName].
 *
 * @see org.gradle.api.plugins.BasePluginExtension.getArchivesBaseName
 * @see org.gradle.api.plugins.BasePluginConvention.getArchivesBaseName
 */
// TODO Provider<String> ?
val Project.archivesBaseNameCompat: String
	get() =
		when {
			GradleVersion.current().baseVersion < GradleVersion.version("7.1") -> {
				@Suppress("DEPRECATION")
				this.convention
					.getPluginByName<org.gradle.api.plugins.BasePluginConvention>("base")
					.archivesBaseName
			}
			else -> {
				// New in Gradle 7.0.
				this.extensions
					.getByName<BasePluginExtension>("base")
					.archivesName
					.get()
			}
		}

/**
 * Gradle 4.3-8.0 compatible version of [Report.getOutputLocation].get().
 *
 * @see Report.getOutputLocation
 * @see Report.getDestination
 */
@Suppress("KDocUnresolvedReference")
fun Report.getOutputLocationCompat(): File =
	when {
		GradleVersion.current().baseVersion < GradleVersion.version("6.1") -> {
			@Suppress("DEPRECATION")
			this.destination
		}
		else -> {
			// New in Gradle 6.1.
			this.outputLocation.get().asFile
		}
	}

/**
 * Gradle 4.3-7.8 compatible version of [ConfigurableReport.getOutputLocation].set().
 *
 * @see ConfigurableReport.setDestination
 * @see ConfigurableReport.getOutputLocation
 */
fun ConfigurableReport.setOutputLocationCompat(destination: Provider<out FileSystemLocation>) {
	when {
		GradleVersion.current().baseVersion < GradleVersion.version("6.1") -> {
			when (this) {
				is SingleFileReport -> {
					@Suppress("DEPRECATION")
					this.destination = destination.get().asFile
				}
				is DirectoryReport -> {
					@Suppress("DEPRECATION")
					this.destination = destination.get().asFile
				}
			}
		}
		else -> {
			// New in Gradle 6.1.
			@Suppress("UNCHECKED_CAST")
			when (this) {
				is SingleFileReport -> outputLocation.set(destination as Provider<RegularFile>)
				is DirectoryReport -> outputLocation.set(destination as Provider<Directory>)
			}
		}
	}
}

/**
 * Polyfill as reflective call, as this method was...
 *  * Added in Gradle 1.0
 *  * [Deprecated in Gradle 7.1](https://github.com/gradle/gradle/commit/85fbb7cd5b7eae14dcff657f712583fcbd225ad6)
 *  * [Removed in Gradle 8.0](https://docs.gradle.org/8.0-rc-1/userguide/upgrading_version_7.html#report_api_cleanup)
 *
 * @see Report.getRequired
 */
@Deprecated(
	message = "Replaced with ConfigurableReport.outputLocation.",
	replaceWith = ReplaceWith("outputLocation.set(value)")
)
var Report.destination: File
	get() {
		val getDestination = Report::class.java.getDeclaredMethod("getDestination")
		return getDestination(this) as File
	}
	set(value) {
		val setDestination = ConfigurableReport::class.java.getDeclaredMethod("setDestination", File::class.java)
		setDestination(this, value)
	}

/**
 * Gradle 4.3-8.0 compatible version of [Report.getOutputLocation].
 *
 * @see ConfigurableReport.setRequired
 * @see ConfigurableReport.setEnabled
 */
@Suppress("KDocUnresolvedReference")
fun ConfigurableReport.setRequired(enabled: Boolean) {
	when {
		GradleVersion.current().baseVersion < GradleVersion.version("6.1") -> {
			@Suppress("DEPRECATION")
			isEnabled = enabled
		}
		else -> {
			required.set(enabled)
		}
	}
}

/**
 * Polyfill as reflective call, as this method was...
 *  * Added in Gradle 1.0
 *  * [Deprecated in Gradle 7.1](https://github.com/gradle/gradle/commit/85fbb7cd5b7eae14dcff657f712583fcbd225ad6)
 *  * [Removed in Gradle 8.0](https://docs.gradle.org/8.0-rc-1/userguide/upgrading_version_7.html#report_api_cleanup)
 *
 * @see ConfigurableReport.getRequired
 */
@Deprecated(
	message = "Replaced with ConfigurableReport.required.",
	replaceWith = ReplaceWith("required.set(value)")
)
var Report.isEnabled: Boolean
	get() {
		val isEnabled = Report::class.java.getDeclaredMethod("isEnabled")
		return isEnabled(this) as Boolean
	}
	set(value) {
		val setEnabled = ConfigurableReport::class.java.getDeclaredMethod("setEnabled", Boolean::class.java)
		setEnabled(this, value)
	}
