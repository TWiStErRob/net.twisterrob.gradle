package net.twisterrob.gradle.test

import net.twisterrob.gradle.test.GradleRunnerRule

object GradleBuildTestResources {

	val kotlin: KotlinProject = object : KotlinProject {}

	interface KotlinProject {
		val build: String
			get() = read("kotlin-plugin_app/build.gradle")
	}

	fun GradleRunnerRule.basedOn(project: KotlinProject) {
		file(project.build, "build.gradle")
	}

	val android: AndroidProject = object : AndroidProject {}

	interface AndroidProject {
		val build: String
			get() = read("android-plugin_app/build.gradle")

		val manifest: String
			get() = read("android-plugin_app/src/main/AndroidManifest.xml")
	}

	fun GradleRunnerRule.basedOn(project: AndroidProject) {
		file(project.build, "build.gradle")
		file(project.manifest, "src", "main", "AndroidManifest.xml")
	}

	private fun read(path: String): String =
		fileFromResources(GradleBuildTestResources::class.java, path)
}

private fun fileFromResources(loader: Class<*>, path: String): String {
	val container = "/${loader.`package`.name.replace(".", "/")}"
	val fullPath = "${container}/${path}"
	val resource = loader.getResource(fullPath)
		?: throw IllegalArgumentException("Cannot find ${fullPath}, trying to load ${path} near ${loader}.")
	return resource.readText()
}
