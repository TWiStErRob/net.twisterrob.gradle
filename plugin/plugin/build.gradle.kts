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
	api("com.android.tools.build:gradle:4.2.2")
	api("org.jetbrains.kotlin:kotlin-reflect:1.4.32")

	implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.32")
	implementation("org.gradle:gradle-kotlin-dsl:6.1.1") {
		isTransitive = false // make sure to not pull in kotlin-compiler-embeddable
	}
}

configurations.all {
	resolutionStrategy.eachDependency {
		val dep: DependencyResolveDetails = this
		// https://github.com/junit-team/junit4/pull/1608#issuecomment-496238766
		if (dep.requested.group == "org.hamcrest") {
			when (dep.requested.name) {
				"java-hamcrest" -> {
					dep.useTarget("org.hamcrest:hamcrest:2.2")
					dep.because("2.0.0.0 shouldn't have been published")
				}

				"hamcrest-core" -> {
					dep.useTarget("org.hamcrest:hamcrest:${dep.target.version}")
					dep.because("hamcrest-core doesn't contain anything")
				}

				"hamcrest-library" -> {
					dep.useTarget("org.hamcrest:hamcrest:${dep.target.version}")
					dep.because("hamcrest-library doesn't contain anything")
				}
			}
		}
	}
}

dependencies { // test
	testImplementation(project(":test"))
	testImplementation("junit:junit:4.13.2") // needed for GradleRunnerRule superclass even when using Extension
	testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
	testImplementation("org.junit.jupiter:junit-jupiter-params:5.8.1")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")

	testImplementation("org.junit-pioneer:junit-pioneer:1.4.2")

	testImplementation("org.hamcrest:hamcrest:2.2") {
		exclude(group = "org.junit", module = "junit")
	}

	testImplementation("org.jetbrains:annotations:22.0.0")
	testImplementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.4.32")
	testImplementation("com.jakewharton.dex:dex-member-list:4.1.1")
}

tasks.named<Jar>("jar") {
	manifest {
		//noinspection UnnecessaryQualifiedReference
		attributes(
			mapOf(
				"Implementation-Vendor" to project.group,
				"Implementation-Title" to project.name,
				"Implementation-Version" to project.version,
				// parsed in net.twisterrob.gradle.builtDate (Global.kt)
				"Built-Date" to DateTimeFormatter.ISO_INSTANT.format(Instant.now())
			)
		)
	}
}

tasks.withType<Test> {
	useJUnitPlatform()

	// Enable verbose test logging, because sometimes AndroidBuildPluginIntgTest hangs, hopefully this will uncover it
	//noinspection UnnecessaryQualifiedReference
	testLogging.events = org.gradle.api.tasks.testing.logging.TestLogEvent.values().toList().toSet()
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

tasks.withType<JavaCompile> {
	targetCompatibility = JavaVersion.VERSION_1_8.toString()
	sourceCompatibility = JavaVersion.VERSION_1_8.toString()
	options.compilerArgs.addAll(
		listOf(
			// check everything
			"-Xlint:all",
			// fail on any warning
			"-Werror",
			//warning: [options] bootstrap class path not set in conjunction with -source 1.7
			"-Xlint:-options",
			//The following annotation processors were detected on the compile classpath:
			// 'javaslang.match.PatternsProcessor'
			// 'com.google.auto.value.extension.memoized.MemoizedValidator'
			// 'com.google.auto.value.processor.AutoAnnotationProcessor'
			// 'com.google.auto.value.processor.AutoValueBuilderProcessor'
			// 'com.google.auto.value.processor.AutoValueProcessor'.
			// implies "-Xlint:-processing",
			"-proc:none"
		)
	)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
	kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
	kotlinOptions.verbose = true
	kotlinOptions.allWarningsAsErrors = true
	kotlinOptions.freeCompilerArgs += listOf(
	)
}
