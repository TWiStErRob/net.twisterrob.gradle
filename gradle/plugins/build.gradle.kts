plugins {
	id("org.gradle.java-gradle-plugin")
	//alias(libs.plugins.kotlin) // Can't apply since there's a mismatch between embedded Kotlin and latest Kotlin.
	`kotlin-dsl`
	alias(libs.plugins.detekt)
	id("org.gradle.idea")
}

// Note on `plugins { }`: when the version is declared in the plugins block (`plugins { id(...) version "..." }`),
// the referenced dependencies are visible by IntelliJ Gradle Sync, but the breakpoints are not hit.
// Declaring all the dependencies in this project resolves this issue.
dependencies {
	implementation(libs.kotlin.gradle)
	implementation(libs.plugins.kotlin.dokka.asDependency())
	implementation(libs.plugins.kotlin.dokkaJavadoc.asDependency())
	implementation(libs.plugins.detekt.asDependency())
	implementation(libs.plugins.lint.asDependency())
	implementation(libs.gradle.develocity)
	implementation(libs.nexus)

	// TODEL https://github.com/gradle/gradle/issues/15383
	implementation(files(libs::class.java.superclass.protectionDomain.codeSource.location))
}

// Reusing code from :plugin:settings in the main project.
run {
	// Rather than directly including the source folder, it's first copied here.
	val sharedCodeFolder: File = file("src/main/kotlin-shared")
	kotlin.sourceSets.named("main").configure { kotlin.srcDir(sharedCodeFolder) }
	// This is to make sure that IDEA doesn't mess things up with duplicated source roots.
	val copyReusableSources = tasks.register<Sync>("copyReusableSources") {
		// More robust: gradle.parent!!.rootProject.project(":plugin:settings").file("src/main/kotlin-shared")
		// but at this point in the lifecycle the parent (including build) rootProject build is not available yet.
		from(rootProject.file("../../plugin/settings/src/main/kotlin-shared"))
		into(sharedCodeFolder)
	}
	// The copied code is marked as generated for IDEA, so it warns when it's accidentally edited.
	idea.module.generatedSourceDirs.add(sharedCodeFolder)
	// The copied code is also excluded, because it keeps showing up in code navigation and search.
	idea.module.excludeDirs.add(sharedCodeFolder)
	// Some tasks will rely on this copied code, so make sure their inputs are appropriately marked.
	tasks.named("compileKotlin").configure { dependsOn(copyReusableSources) }
	tasks.named("detektMain").configure { dependsOn(copyReusableSources) }
	tasks.named("detekt").configure { dependsOn(copyReusableSources) }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
	compilerOptions.verbose = true
	compilerOptions.allWarningsAsErrors = true
}

// Note: duplicated from DetektPlugin because can't apply project this build.gradle.kts is defining.
detekt {
	// TODEL https://github.com/detekt/detekt/issues/4926
	buildUponDefaultConfig = false
	allRules = true
	ignoreFailures = true
	//debug = true
	config.setFrom(
		project.rootProject.file("../../config/detekt/detekt.yml"),
		project.rootProject.file("../../config/detekt/detekt-kotlin-dsl.yml"),
	)
	baseline = project.rootProject.file("../../config/detekt/detekt-baseline-gradle-plugins.xml")
	basePath = project.rootProject.projectDir.resolve("../..").absolutePath

	parallel = true

	project.tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
		reports {
			html.required = true // human
			txt.required = true // console
		}
		if (this.name == "detektMain") {
			// Detekt fails on these files with an internal compile error, so exclude for now.
			exclude("**/net.twisterrob.gradle.build.*.gradle.kts")
			// Cannot do much about the violations in Gradle generated code; also causes OOM.
			// build/generated-sources/kotlin-dsl-accessors/kotlin/gradle/kotlin/dsl/accessors/
			exclude("gradle/kotlin/dsl/accessors/")
			// build/generated-sources/kotlin-dsl-external-plugin-spec-builders/kotlin/gradle/kotlin/dsl/plugins/
			exclude("gradle/kotlin/dsl/plugins/")
			// build/generated-sources/kotlin-dsl-plugins/kotlin/Net_twisterrob_gradle_build_*Plugin
			exclude("Net_twisterrob_gradle_build_*Plugin.kt")
		}
	}
}

fun Provider<PluginDependency>.asDependency(): Provider<String> =
	this.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }
