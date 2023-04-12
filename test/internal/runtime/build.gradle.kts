plugins {
	id("net.twisterrob.gradle.build.module.library")
}

dependencies {
	compileOnly(gradleApi())
	implementation(projects.plugin.settings)
}
