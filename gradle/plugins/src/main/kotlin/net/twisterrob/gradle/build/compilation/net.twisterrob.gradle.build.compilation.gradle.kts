import net.twisterrob.gradle.build.dsl.libs
import org.gradle.api.tasks.compile.GroovyCompile
import org.gradle.api.tasks.compile.JavaCompile
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
	compileOnly(enforcedPlatform(libs.kotlin.bom))
	// Allow tests to use latest.
	testImplementation(enforcedPlatform(libs.kotlin.bom)) {
		version { require(libs.versions.kotlin.build.get()) }
	}
}

java {
	sourceCompatibility = JavaVersion.toVersion(libs.versions.java.get())
	targetCompatibility = JavaVersion.toVersion(libs.versions.java.get())
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
	kotlinOptions.verbose = true
	kotlinOptions.languageVersion = libs.versions.kotlin.language.get()
	kotlinOptions.apiVersion = libs.versions.kotlin.language.get()
	kotlinOptions.jvmTarget = libs.versions.java.get()
	kotlinOptions.suppressWarnings = false
	kotlinOptions.allWarningsAsErrors = true
	kotlinOptions.freeCompilerArgs += listOf(
		// Caused by: java.lang.NoSuchMethodError: kotlin.jvm.internal.FunctionReferenceImpl.<init>(ILjava/lang/Object;Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;I)V
		//	at net.twisterrob.gradle.common.BaseQualityPlugin$apply$1$1.<init>(BaseQualityPlugin.kt)
		//	at net.twisterrob.gradle.common.BaseQualityPlugin$apply$1.execute(BaseQualityPlugin.kt:24)
		//	at net.twisterrob.gradle.common.BaseQualityPlugin$apply$1.execute(BaseQualityPlugin.kt:8)
		// https://youtrack.jetbrains.com/issue/KT-41852#focus=Comments-27-4604992.0-0
		"-Xno-optimized-callable-references",
		"-opt-in=kotlin.RequiresOptIn",
	)
	if (kotlinOptions.languageVersion == "1.4") {
		// Suppress "Language version 1.4 is deprecated and its support will be removed in a future version of Kotlin".
		kotlinOptions.freeCompilerArgs += "-Xsuppress-version-warnings"
	} else {
		TODO("Remove -Xsuppress-version-warnings")
	}
}
