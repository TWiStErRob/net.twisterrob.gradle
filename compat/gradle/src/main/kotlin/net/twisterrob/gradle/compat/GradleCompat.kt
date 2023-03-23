@file:Suppress(
	"TooManyFunctions", // TODO might be worth splitting by receiver and use @JvmMultifileClass?
	"UseIfInsteadOfWhen", // Preparing for future new version ranges.
	"MaxLineLength", // OK for comments with links.
	"UNUSED_PARAMETER", // STOPSHIP remove
)

package net.twisterrob.gradle.compat

import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.reporting.ConfigurableReport
import org.gradle.api.reporting.DirectoryReport
import org.gradle.api.reporting.Report
import org.gradle.api.reporting.SingleFileReport
import org.gradle.util.GradleVersion
import java.io.File

/**
 * @param T the input type
 * @param S the output type, may be nullable; upon returning null the provider will have no value.
 * @param transformer The transformer to apply to values.
 */
fun <T, S> Provider<T>.flatMapCompat(transformer: (T) -> Provider<S>): Provider<S> =
	this.flatMap(transformer) // STOPSHIP inline

/**
 * Gradle 4.3-6.9 compatible version of [ObjectFactory.fileProperty].
 *
 * @see RegularFileProperty.set
 * @see RegularFileProperty.convention
 */
fun RegularFileProperty.conventionCompat(file: Provider<RegularFile>): RegularFileProperty =
	this.convention(file) // STOPSHIP inline

/**
 * Gradle 4.3-6.9 compatible version of [RegularFileProperty.fileProvider].
 *
 * @see RegularFileProperty.set
 * @see RegularFileProperty.fileProvider
 */
fun RegularFileProperty.fileProviderCompat(task: DefaultTask, file: Provider<File>): RegularFileProperty =
	this.fileProvider(file) // STOPSHIP inline

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
	this.fileProperty() // STOPSHIP inline

/**
 * Gradle 4.3-8.0 compatible version of [Report.getOutputLocation].get().
 *
 * @see Report.getOutputLocation
 * @see Report.getDestination
 */
@Suppress("KDocUnresolvedReference")
fun Report.getOutputLocationCompat(): File =
	when {
		GradleVersion.current().baseVersion < GradleVersion.version("8.0") -> {
			// New in Gradle 6.1 with return type Provider<? extends FileSystemLocation>.
			this.outputLocationCompat.get().asFile
		}
		else -> {
			// Return type changed in Gradle 8.0 to Property<? extends FileSystemLocation>.
			this.outputLocation.get().asFile
		}
	}

/**
 * Reflective access for the breaking change.
 * * [Return type of `outputLocation` changed in Gradle 8.0](https://docs.gradle.org/8.0-rc-1/userguide/upgrading_version_7.html#report_getoutputlocation_return_type_changed_from_provider_to_property).
 *   [PR](https://github.com/gradle/gradle/pull/22232).
 *
 * @see Report.getOutputLocation
 */
private val Report.outputLocationCompat: Provider<out FileSystemLocation>
	get() {
		val method = Report::class.java.getDeclaredMethod("getOutputLocation")
		@Suppress("UNCHECKED_CAST")
		return method(this) as Provider<out FileSystemLocation>
	}

/**
 * Gradle 4.3-7.8 compatible version of [ConfigurableReport.getOutputLocation].set().
 *
 * @see ConfigurableReport.setDestination
 * @see ConfigurableReport.getOutputLocation
 */
fun ConfigurableReport.setOutputLocationCompat(destination: Provider<out FileSystemLocation>) {
	// STOPSHIP inline or explicit condition?
	// Even though return value changed in Gradle 8.0, the smart cast makes it safe.
	@Suppress("UNCHECKED_CAST")
	when (this) {
		is SingleFileReport -> outputLocation.set(destination as Provider<RegularFile>)
		is DirectoryReport -> outputLocation.set(destination as Provider<Directory>)
	}
}

/**
 * Gradle 4.3-8.0 compatible version of [Report.getRequired].
 *
 * @see ConfigurableReport.setRequired
 * @see ConfigurableReport.setEnabled
 */
@Suppress("KDocUnresolvedReference")
fun ConfigurableReport.setRequired(enabled: Boolean) {
	required.set(enabled) // STOPSHIP inline
}
