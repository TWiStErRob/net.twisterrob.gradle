plugins {
	kotlin
	`java-library`
	id("net.twisterrob.gradle.build.publishing")
}

base.archivesName.set("twister-compat-agp-7.4.x")
description = "AGP Compatibility 7.4.x: Compatibility layer for Android Gradle Plugin 7.4.x."

dependencies {
	implementation(gradleApiWithoutKotlin())
	compileOnly(libs.android.gradle.v74x)
}

plugins.withId("java") {
	// libs.android.gradle.v74x declares `org.gradle.jvm.version=11` as an attribute,
	// which fails to resolve when using Java 8.
	// Details: `gradlew :compat:agp-74x:dependencyInsight --dependency=com.android.tools.build:gradle`
	val java = extensions.getByName<JavaPluginExtension>("java")
	java.sourceCompatibility = JavaVersion.VERSION_11
	java.targetCompatibility = JavaVersion.VERSION_11
}

// https://docs.gradle.org/current/javadoc/org/gradle/api/artifacts/DependencySubstitutions.html
// https://docs.gradle.org/current/userguide/variant_model.html
// https://discuss.gradle.org/t/why-does-my-consumer-prefer-java-8-variant-to-java-11-variant-while-building-with-java-11/39194
// https://discuss.gradle.org/t/why-am-i-getting-this-no-matching-variant-failure-message/39248
// TODO https://discuss.gradle.org/t/is-it-possible-to-determine-consumer-java-version-in-library/39184
// FIXME https://docs.gradle.org/current/userguide/component_metadata_rules.html
configurations.all {
	attributes {
		attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 8)
	}
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
	kotlinOptions.freeCompilerArgs += listOf(
		// e: Incompatible classes were found in dependencies.
		// Remove them from the classpath or use '-Xskip-metadata-version-check' to suppress errors
		// e: gradle/caches/modules-2/files-2.1/com.android.tools.build/.../builder-model-7.4.0-alpha09.jar!/META-INF/builder-model.kotlin_module:
		// Module was compiled with an incompatible version of Kotlin.
		// The binary version of its metadata is 1.6.0, expected version is 1.4.2.
		//e: compat\agp-74x\src\main\kotlin\net\twisterrob\gradle\internal\android\AndroidHelpers-taskContainerCompat74x.kt: (3, 48):
		// Class 'com.android.build.gradle.internal.scope.TaskContainer' was compiled with an incompatible version of Kotlin.
		// The binary version of its metadata is 1.6.0, expected version is 1.4.2
		"-Xskip-metadata-version-check"
	)
}

