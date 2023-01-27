plugins {
	id("net.twisterrob.gradle.build.module.library")
}

dependencies {
	compileOnly(gradleApiWithoutKotlin())
	implementation(projects.plugin.settings)
}

if (project.property("net.twisterrob.gradle.build.hackKotlinMetadata") == "true") {
	tasks.jar {
		exclude("META-INF/runtime.kotlin_module")
	}
}
