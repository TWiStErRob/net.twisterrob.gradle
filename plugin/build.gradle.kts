plugins {
	kotlin
	id("java-gradle-plugin")
	id("net.twisterrob.gradle.build.publishing")
}

base.archivesName.set("twister-convention-plugins")
description = "Convention Plugins: Gradle Plugins used by my hobby projects."

gradlePlugin {
	plugins {
		create("net.twisterrob.root") {
			id = "net.twisterrob.root"
			implementationClass = "net.twisterrob.gradle.base.BaseExposedPlugin"
		}
		create("net.twisterrob.vcs") {
			id = "net.twisterrob.vcs"
			implementationClass = "net.twisterrob.gradle.vcs.VCSPlugin"
		}

		create("net.twisterrob.java") {
			id = "net.twisterrob.java"
			implementationClass = "net.twisterrob.gradle.java.JavaPlugin"
		}
		create("net.twisterrob.java-library") {
			id = "net.twisterrob.java-library"
			implementationClass = "net.twisterrob.gradle.java.JavaLibPlugin"
		}
		create("net.twisterrob.kotlin") {
			id = "net.twisterrob.kotlin"
			implementationClass = "net.twisterrob.gradle.kotlin.KotlinPlugin"
		}

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
	implementation(projects.common)
	implementation(Libs.SVNKit.svnkit)
	implementation(Libs.SVNKit.`svnkit.cli`)
	implementation(Libs.jgit)
	compileOnly(Libs.Android.agp)
	compileOnly(Libs.Kotlin.`kotlin.gradle`)

	testImplementation(projects.test.internal)
	testImplementation(Libs.JUnit5.`junit.pioneer`)
	testImplementation(Libs.dexMemberList)
	testCompileOnly(Libs.Android.agp)
	// GradleTestHelpersKt.hasDevices uses com.android.ddmlib.AndroidDebugBridge.
	testImplementation(Libs.Android.toolsDdmLib)
	// AndroidInstallRunnerTaskTest calls production code directly,
	// so need com.android.xml.AndroidXPathFactory for AndroidInstallRunnerTask.Companion.getMainActivity$plugin.
	testImplementation(Libs.Android.toolsCommon)
}

tasks.withType<Test> {
	// See GradleTestKitDirRelocator for what enables this!
	maxParallelForks = 10
	// Limit memory usage of test forks. Gradle <5 allows 1/4th of total memory to be used, thus forbidding many forks.
	// Memory limit for the :plugin:test task running JUnit tests.
	// The Gradle builds use the default in DaemonParameters.
	maxHeapSize = "256M"
	onlyIf {
		it.project.findProperty("net.twisterrob.test.android.pluginVersion").toString() >= "4.0.0"
	}
}

tasks.named<PluginUnderTestMetadata>("pluginUnderTestMetadata") {
	// In Gradle 6.5.1 to 6.6 upgrade something changed.
	// The folders on the classpath
	// classpath files('net.twisterrob.gradle\\plugin\\plugin\\build\\classes\\java\\main')
	// classpath files('net.twisterrob.gradle\\plugin\\plugin\\build\\classes\\kotlin\\main')
	// classpath files('net.twisterrob.gradle\\plugin\\plugin\\build\\resources\\main')
	// are now used as a quickly ZIPped JAR file
	// file:/Temp/.gradle-test-kit-TWiStEr-6/caches/jars-8/612d2cded1e3015b824ce72a63bd2fb6/main.jar
	// but this is missing the MANIFEST.MF file, as only the class and resource files are there.
	// Adding the temporary directory for the manifest is not enough like this:
	// it.pluginClasspath.from(files(file("build/tmp/jar/")))
	// because it needs to be in the same JAR file as the class files.
	// To work around this: prepend the final JAR file on the classpath:
	val jar = tasks.named<Jar>("jar").get()
	pluginClasspath.setFrom(files(jar.archiveFile) + pluginClasspath)
}
