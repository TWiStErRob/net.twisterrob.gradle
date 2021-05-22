pluginManagement {
	repositories {
		google()
		mavenCentral()
		gradlePluginPortal()
	}
	resolutionStrategy {
		eachPlugin {
			when (requested.id.id) {
				"com.android.application" ->
					useModule("com.android.tools.build:gradle:${requested.version}")
				"net.twisterrob.quality" ->
					useModule("net.twisterrob.gradle:twister-quality:${requested.version}")
			}
		}
	}
}
