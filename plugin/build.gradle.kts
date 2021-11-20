plugins {
	kotlin
	id("java-gradle-plugin")
	id("net.twisterrob.gradle.build.publishing")
}

base.archivesName.set("twister-convention-plugins")
description = "Convention Plugins: Gradle Plugins used by my hobby projects."

gradlePlugin {
	plugins {
		create("net.twisterrob.android-app") {
			id = "net.twisterrob.android-app"
			implementationClass = "net.twisterrob.gradle.android.AndroidAppPlugin"
		}
		create("net.twisterrob.android-library") {
			id = "net.twisterrob.android-library"
			implementationClass = "net.twisterrob.gradle.android.AndroidLibraryPlugin"
		}
		create("net.twisterrob.android-test") {
			id = "net.twisterrob.android-test"
			implementationClass = "net.twisterrob.gradle.android.AndroidTestPlugin"
		}
	}
}

dependencies {
	implementation(gradleApiWithoutKotlin())
	implementation(projects.plugin.base)

	api(projects.plugin.versioning)
	api(projects.plugin.signing)
	api(projects.plugin.languages)
	api(projects.plugin.release)
	api(projects.plugin.building)
	api(projects.plugin.reporting)

	compileOnly(libs.annotations.jetbrains)
	compileOnly(libs.android.gradle)
	compileOnly(libs.kotlin.gradle)

	testImplementation(projects.test.internal)
	testImplementation(testFixtures(projects.plugin.base))
	testCompileOnly(libs.android.gradle)
	// AndroidInstallRunnerTaskTest calls production code directly,
	// so need com.android.xml.AndroidXPathFactory for AndroidInstallRunnerTask.Companion.getMainActivity$plugin.
	testImplementation(libs.android.tools.common)
}

tasks.withType<Test>().configureEach {
	// See GradleTestKitDirRelocator for what enables this!
	maxParallelForks = 10
	// Limit memory usage of test forks. Gradle <5 allows 1/4th of total memory to be used, thus forbidding many forks.
	// Memory limit for the :plugin:test task running JUnit tests.
	// The Gradle builds use the default in DaemonParameters.
	maxHeapSize = "256M"
}

allprojects {
	tasks.withType<Test>().configureEach {
		onlyIf {
			it.project.findProperty("net.twisterrob.test.android.pluginVersion").toString() >= "4.0.0"
		}
	}

	afterEvaluate {
		try {
			addJarToClasspathOfPlugin()
		} catch (ex: UnknownTaskException) {
			if (ex.message?.startsWith("Task with name 'pluginUnderTestMetadata' not found in project ") == true) {
				// All good, ignore.
			} else {
				throw ex
			}
		}
	}
}
