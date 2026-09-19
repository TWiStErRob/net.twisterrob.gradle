import org.gradle.api.internal.catalog.parser.TomlCatalogFileParser
import org.gradle.kotlin.dsl.support.serviceOf

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
