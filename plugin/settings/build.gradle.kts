plugins {
	id("net.twisterrob.gradle.build.module.gradle-plugin")
	id("net.twisterrob.gradle.build.publish")
	id("idea")
}

base.archivesName.set("twister-convention-settings")
description = "Settings Convention Plugin: Gradle Plugin to apply in settings.gradle files."

gradlePlugin {
	plugins {
		create("net.twisterrob.settings") {
			id = "net.twisterrob.settings"
			implementationClass = "net.twisterrob.gradle.settings.SettingsPlugin"
		}
	}
}

// Reusing code from /gradle/plugins included build and publishing it as part of this module's JAR.
// Rather than directly including the source folder, it's first copied here.
val sharedCodeFolder: File = file("src/main/kotlin-reused")
kotlin.sourceSets.named("main").configure { kotlin.srcDir(sharedCodeFolder) }
// The copied code is marked as generated for IDEA, so it warns when it's accidentally edited.
idea.module.generatedSourceDirs.add(sharedCodeFolder)
// This is to make sure that IDEA doesn't mess things up with duplicated source roots.
val copyReusableSources = tasks.register<Copy>("copyReusableSources") {
	// Need to hard-code path, no better sharing mechanism found yet: https://stackoverflow.com/q/73557522/253468
	from(rootProject.file(gradle.includedBuild("plugins").projectDir.resolve("src/main/kotlin-published")))
	into(sharedCodeFolder)
}
tasks.named("compileKotlin").configure { dependsOn(copyReusableSources) }
tasks.named("detektMain").configure { dependsOn(copyReusableSources) }
tasks.named("detekt").configure { dependsOn(copyReusableSources) }

dependencies {
	implementation(gradleApiWithoutKotlin())
	// Expose some methods to TestKit runtime classpath.
	implementation(projects.compat.gradle)

	testImplementation(projects.test.internal)
	testImplementation(testFixtures(projects.plugin.base))
}
