tasks.register("check") {
	description = "Delegate task for checking included builds too."
	dependsOn(gradle.includedBuild("plugins").task(":check"))
}

if (providers.gradleProperty("net.twisterrob.gradle.build.includeExamples").map(String::toBoolean).get()) {
	tasks.register("assembleExamples") {
		dependsOn(gradle.includedBuilds.map { it.task(":assemble") })
	}
	tasks.register("checkExamples") {
		dependsOn(gradle.includedBuilds.map { it.task(":check") })
	}
}

project.tasks.register<Delete>("cleanDebug") {
	group = LifecycleBasePlugin.VERIFICATION_GROUP
	description = "Clean outputs generated by debug projects."
	// Hacky version of the following with intuitive results (i.e. the folders are also deleted):
	// ```
	// delete(fileTree(rootProject.file("docs/debug")) {
	//     include("*/.gradle")
	//     include("*/build")
	//     include("*/buildSrc/.gradle")
	//     include("*/buildSrc/build")
	// })
	// ```
	// See https://github.com/gradle/gradle/issues/14152#issuecomment-953610543.
	fileTree(rootProject.file("docs/debug")) {
		include("*/.gradle")
		include("*/buildSrc/.gradle")
	}.visit { if (name == ".gradle") delete(file) }
	fileTree(rootProject.file("docs/debug")) {
		include("*/build")
		include("*/buildSrc/build")
	}.visit { if (name == "build") delete(file) }
}
