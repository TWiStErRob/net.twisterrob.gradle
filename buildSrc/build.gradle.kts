buildscript {
	val props by extra {
		java.util.Properties().apply {
			file("../gradle.properties").reader().use { load(it) }
		}
	}
	val VERSION_ANDROID_PLUGIN by props
	repositories {
		google()
		jcenter()
	}

	dependencies {
		classpath(kotlin("gradle-plugin"))
		// to be able to access com.android.builder.model.Version artifact
		classpath("com.android.tools.build:builder-model:${VERSION_ANDROID_PLUGIN}")
	}
}

plugins {
	`kotlin-dsl`
}

repositories {
	google()
	jcenter()
}

// don't want to use this, just so IDEA puts it in the project for source browsing
val sourcesOnly by configurations.creating
// hack to actually make Gradle resolve and use this
configurations.compileOnly.extendsFrom(sourcesOnly)

dependencies {
	implementation(kotlin("gradle-plugin"))
	sourcesOnly("com.android.tools.lint:lint:${com.android.builder.model.Version.ANDROID_TOOLS_BASE_VERSION}")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
	// TODO Using kotlin incremental compilation shows up regardless of verbose
	kotlinOptions.verbose = true
//	kotlinOptions.allWarningsAsErrors = true
	kotlinOptions.freeCompilerArgs += listOf(
			// w: Some JAR files in the classpath have the Kotlin Runtime library bundled into them.
			// This may cause difficult to debug problems if there's a different version of the Kotlin Runtime library in the classpath.
			// w: ...\org.jetbrains.kotlin\kotlin-compiler-embeddable\1.1.4-3\...\kotlin-compiler-embeddable-1.1.4-3.jar:
			// Library has Kotlin runtime bundled into it
			"-Xskip-runtime-version-check"
	)
}
