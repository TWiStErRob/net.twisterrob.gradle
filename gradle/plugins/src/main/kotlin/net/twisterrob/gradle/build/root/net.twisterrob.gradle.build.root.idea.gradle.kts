plugins {
	id("org.gradle.idea")
}

idea {
	module {
		fun excludedInProject(dir: File): List<File> =
			listOf(
				dir.resolve(".gradle"),
				dir.resolve("build"),
				dir.resolve("buildSrc/.gradle"),
				dir.resolve("buildSrc/build"),
				dir.resolve(".idea"),
			)

		val examples = listOf(
			"docs/examples/local",
			"docs/examples/release",
			"docs/examples/snapshot",
			"graph/sample",
		)
			.map(rootDir::resolve)
			.flatMap(::excludedInProject)
		val debuggers = rootDir
			.resolve("docs/debug")
			.listFiles { file: File -> file.isDirectory }
			.flatMap(::excludedInProject)
		val unpackagedResources = allprojects.map { it.projectDir.resolve("build/unPackagedTestResources") }
		excludeDirs.addAll(examples + debuggers + unpackagedResources)
	}
}
