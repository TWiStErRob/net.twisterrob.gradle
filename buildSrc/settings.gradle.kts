// TODEL https://github.com/gradle/gradle/issues/18975
enableFeaturePreview("VERSION_CATALOGS")

dependencyResolutionManagement {
	@Suppress("UnstableApiUsage")
	versionCatalogs {
		create("deps") {
			from(files("../gradle/libs.versions.toml"))
		}
	}
}
