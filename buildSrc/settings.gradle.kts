dependencyResolutionManagement {
	@Suppress("UnstableApiUsage")
	versionCatalogs {
		// TODEL use libs, see https://github.com/gradle/gradle/issues/18999 / https://youtrack.jetbrains.com/issue/KTIJ-20166
		create("projectLibs") {
			from(files("../gradle/libs.versions.toml"))
		}
	}
}
