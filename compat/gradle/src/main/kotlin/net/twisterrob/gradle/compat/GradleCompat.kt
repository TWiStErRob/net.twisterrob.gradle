@file:Suppress(
	"TooManyFunctions", // TODO might be worth splitting by receiver and use @JvmMultifileClass?
	"UseIfInsteadOfWhen", // Preparing for future new version ranges.
	"MaxLineLength", // OK for comments with links.
)

package net.twisterrob.gradle.compat

import org.gradle.api.file.Directory
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.reporting.ConfigurableReport
import org.gradle.api.reporting.DirectoryReport
import org.gradle.api.reporting.Report
import org.gradle.api.reporting.SingleFileReport
import org.gradle.util.GradleVersion
import java.io.File

/**
 * Gradle 6.1-8.0 compatible version of [Report.getOutputLocation].get().
 *
 * @see Report.getOutputLocation
 * @see Report.getDestination
 */
@Suppress("KDocUnresolvedReference")
fun Report.getOutputLocationCompat(): File = // STOPSHIP provider
	when {
		GradleVersion.version("8.0") <= GradleVersion.current().baseVersion -> {
			// Return type changed in Gradle 8.0 to Property<? extends FileSystemLocation>.
			this.outputLocation.get().asFile
		}
		GradleVersion.version("6.1") <= GradleVersion.current().baseVersion -> {
			// New in Gradle 6.1 with return type Provider<? extends FileSystemLocation>.
			this.outputLocationCompat.get().asFile
		}
		else -> error("Lowest supported version is Gradle 7.0")
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
