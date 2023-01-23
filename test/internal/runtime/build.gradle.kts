import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
	id("net.twisterrob.gradle.build.module.library")
}

dependencies {
	compileOnly(gradleApiWithoutKotlin())
	implementation(projects.plugin.settings)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
	@Suppress("DEPRECATION") // Kotlin 1.4 is deprecated,
	// but this repo supports Gradle 4.9 which is on Kotlin 1.3.
	// https://docs.gradle.org/current/userguide/compatibility.html#kotlin
	// Without a low version, tests using old Gradle (e.g. VersionsTaskTest) fail with:
	// > init.gradle.kts: Class 'NaggingPlugin' was compiled with an incompatible version of Kotlin.
	// > The binary version of its metadata is 1.4.3, expected version is 1.1.10.
	// > The class is loaded from /.../caches/jars-3/<hash>/runtime-0.15-SNAPSHOT.jar!/.../NaggingPlugin.class
	// > e: Incompatible classes were found in dependencies.
	// > Remove them from the classpath or use '-Xskip-metadata-version-check' to suppress errors
	val oldestRuntimeVersion = KotlinVersion.KOTLIN_1_3
	compilerOptions {
		languageVersion.set(oldestRuntimeVersion)
		apiVersion.set(oldestRuntimeVersion)
	}
}
