plugins {
	id("net.twisterrob.gradle.build.module.library")
}

dependencies {
	compileOnly(gradleApiWithoutKotlin())
}
