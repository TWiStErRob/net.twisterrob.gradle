import org.gradle.api.internal.catalog.parser.TomlCatalogFileParser
import org.gradle.kotlin.dsl.support.serviceOf

dependencyResolutionManagement {
	repositories {
		repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
		mavenCentral()
		gradlePluginPortal()
	}
	versionCatalogs {
		create(defaultLibrariesExtensionName.get()) {
			from(files("../../gradle/libs.versions.toml"))
			// Note: it is necessary to load both libs and agp the same way as it's done
			// in rootProject.settings.gradle.kts, because otherwise there are unresolved references.
			TomlCatalogFileParser.parse(file("../../gradle/agp.versions.toml").toPath(), this) { settings.serviceOf<Problems>() }
		}
	}
}
