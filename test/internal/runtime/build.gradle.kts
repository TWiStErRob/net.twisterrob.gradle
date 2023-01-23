plugins {
	id("net.twisterrob.gradle.build.module.library")
}

dependencies {
	compileOnly(gradleApiWithoutKotlin())
	implementation(projects.plugin.settings)
}
