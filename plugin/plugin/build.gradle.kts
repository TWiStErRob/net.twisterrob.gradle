import java.time.Instant
import java.time.format.DateTimeFormatter

/*
dependencies {
	implementation("net.twisterrob.gradle:twister-gradle-test:0.11")
	implementation(kotlin("gradle-plugin", version = kotlin_version))
	implementation(enforcedPlatform("org.jetbrains.kotlin:kotlin-bom:${kotlin_version}"))
	implementation(kotlin("compiler-embeddable", version = kotlin_version))
}
subprojects {
	repositories {
		maven { name = "Gradle libs (for Kotlin-DSL)"; setUrl("https://repo.gradle.org/gradle/libs-releases-local/") }
	}
}

 */
plugins {
	kotlin
	id("java-gradle-plugin")
}

version = "4.2.2.14-30-30.0"

dependencies { // last checked 2020-11-04 (all latest, except Gradle+Kotlin)
	implementation(gradleApi())

	// https://mvnrepository.com/artifact/org.tmatesoft.svnkit/svnkit
	implementation("org.tmatesoft.svnkit:svnkit:1.10.3")
	implementation("org.tmatesoft.svnkit:svnkit-cli:1.10.3")

	// Version history: https://mvnrepository.com/artifact/org.eclipse.jgit/org.eclipse.jgit
	// Changelog (Full): https://projects.eclipse.org/projects/technology.jgit
	// Changelog (Summary): https://wiki.eclipse.org/JGit/New_and_Noteworthy
	implementation("org.eclipse.jgit:org.eclipse.jgit:5.13.0.202109080827-r")

	// https://developer.android.com/studio/releases/gradle-plugin.html#updating-gradle
	compileOnly(Libs.Android.plugin)

	implementation(Libs.Kotlin.gradlePlugin)
}

dependencies { // test
	testImplementation(project(":test"))

	testImplementation("org.junit-pioneer:junit-pioneer:1.4.2")

	testImplementation("com.jakewharton.dex:dex-member-list:4.1.1")
}

tasks.withType<Test> {
	useJUnitPlatform()

	// See GradleTestKitDirRelocator for what enables this!
	maxParallelForks = 10
	// Limit memory usage of test forks. Gradle <5 allows 1/4th of total memory to be used, thus forbidding many forks.
	maxHeapSize = "256M"
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
