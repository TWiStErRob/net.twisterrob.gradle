import net.twisterrob.gradle.build.dsl.libs
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.jetbrains.kotlin.jvm")
}

dependencies {
	api(libs.kotlin.stdlib)
	api(libs.kotlin.stdlib.jdk8)
	api(libs.kotlin.reflect)
	compileOnly(libs.kotlin.dsl) {
		isTransitive = false // make sure to not pull in kotlin-compiler-embeddable
	}
	// Make sure we don't have many versions of Kotlin lying around.
	compileOnly(platform(libs.kotlin.bom))
	// Allow tests to use latest.
	testImplementation(platform(libs.kotlin.bom)) {
		version { require(libs.versions.kotlin.build.get()) }
	}
}

java {
	sourceCompatibility = libs.versions.java.map(JavaVersion::toVersion).get()
	targetCompatibility = libs.versions.java.map(JavaVersion::toVersion).get()
	// Cannot use this, because test Kotlin needs to be different.
	//consistentResolution { useCompileClasspathVersions() }
}

tasks.withType<JavaCompile>().configureEach {
	options.compilerArgs.addAll(
		listOf(
			"-Werror", // fail on warnings
			"-Xlint:all", // enable all possible checks
			"-Xlint:-processing" // except "No processor claimed any of these annotations"
		)
	)
}

tasks.withType<GroovyCompile>().configureEach {
	options.compilerArgs.addAll(
		listOf(
			"-Werror", // fail on warnings
			"-Xlint:all" // enable all possible checks
		)
	)
	groovyOptions.configurationScript = rootProject.file("gradle/compileGroovy.groovy")
	// enable Java 7 invokeDynamic, since Java target is > 7 (Android requires Java 8 at least)
	// no need for groovy-all:ver-indy, because the classpath is provided from hosting Gradle project
	groovyOptions.optimizationOptions!!["indy"] = true
}

tasks.withType<KotlinCompile>().configureEach {
	compilerOptions {
		verbose = true
		languageVersion = libs.versions.kotlin.language.map(KotlinVersion::fromVersion)
		apiVersion = libs.versions.kotlin.language.map(KotlinVersion::fromVersion)
		jvmTarget = libs.versions.java.map(JvmTarget::fromTarget)
		suppressWarnings = false
		allWarningsAsErrors = true
		freeCompilerArgs.addAll(
			// Opt in to https://youtrack.jetbrains.com/issue/KT-59109 for now to see how to suppress warnings/errors.
			"-Xrender-internal-diagnostic-names",
		)
	}
}
