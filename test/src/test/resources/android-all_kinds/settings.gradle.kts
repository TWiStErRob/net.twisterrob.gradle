include(":library")
include(":library:nested")
include(":dynamic_feature")
include(":app")
include(":test")

dependencyResolutionManagement {
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
	repositories {
		google()
		mavenCentral()
	}
}
