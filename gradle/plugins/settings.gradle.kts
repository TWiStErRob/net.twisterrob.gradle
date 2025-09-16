import net.twisterrob.gradle.doNotNagAbout
import org.gradle.api.internal.catalog.parser.TomlCatalogFileParser
import org.gradle.kotlin.dsl.support.serviceOf

plugins {
	id("net.twisterrob.gradle.plugin.nagging") version "0.18"
}

dependencyResolutionManagement {
	@Suppress("UnstableApiUsage")
	repositories {
		repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
		mavenCentral()
		gradlePluginPortal()
		google()
	}
	versionCatalogs {
		create(defaultLibrariesExtensionName.get()) {
			from(files("../../gradle/libs.versions.toml"))
			// Note: it is necessary to load both libs and agp the same way as it's done
			// in rootProject.settings.gradle.kts, because otherwise there are unresolved references.
			load("../../gradle/agp.versions.toml")
			load("../../gradle/kotlin.versions.toml")
		}
	}
}

@Suppress("UnstableApiUsage")
fun VersionCatalogBuilder.load(path: String) {
	TomlCatalogFileParser.parse(file(path).toPath(), this) { settings.serviceOf<Problems>() }
}

val gradleVersion: String = GradleVersion.current().version

// TODEL Gradle 9.1 vs detekt 1.23.8 https://github.com/detekt/detekt/issues/8452
@Suppress("detekt.MaxLineLength")
doNotNagAbout(
	"The ReportingExtension.file(String) method has been deprecated. " +
			"This is scheduled to be removed in Gradle 10. " +
			"Please use the getBaseDirectory().file(String) or getBaseDirectory().dir(String) method instead. " +
			"Consult the upgrading guide for further information: " +
			"https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_9.html#reporting_extension_file",
	"at io.gitlab.arturbosch.detekt.DetektPlugin.apply(DetektPlugin.kt:28)",
)
