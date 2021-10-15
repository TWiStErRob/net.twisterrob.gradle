plugins {
	kotlin
	id("java-gradle-plugin")
	id("net.twisterrob.gradle.build.publishing")
}

version = "4.2.2.14-30-30.0"
description = "Private Plugin: Gradle Plugin used by my hobby projects."

dependencies {
	implementation(gradleApiWithoutKotlin())
	implementation(project(":common"))
	implementation(Libs.SVNKit.core)
	implementation(Libs.SVNKit.cli)
	implementation(Libs.jgit)
	compileOnly(Libs.Android.plugin)
	compileOnly(Libs.Kotlin.gradlePlugin)

	testImplementation(project(":test"))
	testImplementation(project(":test:internal"))
	testImplementation(Libs.JUnit5.pioneer)
	testImplementation(Libs.dexMemberList)
	testCompileOnly(Libs.Android.plugin)
	// GradleTestHelpersKt.hasDevices uses com.android.ddmlib.AndroidDebugBridge.
	testImplementation(Libs.Android.toolsDddmlib)
	// AndroidInstallRunnerTaskTest calls production code directly,
	// so need com.android.xml.AndroidXPathFactory for AndroidInstallRunnerTask.Companion.getMainActivity$plugin.
	testImplementation(Libs.Android.toolsCommon)
}

tasks.withType<Test> {
	// See GradleTestKitDirRelocator for what enables this!
	maxParallelForks = 10
	// Limit memory usage of test forks. Gradle <5 allows 1/4th of total memory to be used, thus forbidding many forks.
	// Note: 256M was enough for a long time, but GitHub Actions quite consistently fails with
	// java.lang.OutOfMemoryError: Metaspace in AndroidBuildPluginIntgTest.`can override minSdkVersion (release)`()
	maxHeapSize = "384M"
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
