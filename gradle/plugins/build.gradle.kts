plugins {
	id("org.gradle.java-gradle-plugin")
	//alias(libs.plugins.kotlin) // Can't apply since there's a mismatch between embedded Kotlin and latest Kotlin.
	`kotlin-dsl`
	alias(libs.plugins.detekt)
	id("idea")
}

gradlePlugin {
	plugins {
		create("settings") {
			id = "net.twisterrob.gradle.build.settings"
			implementationClass = "net.twisterrob.gradle.plugins.settings.SettingsPlugin"
		}

		// Re-exposure of plugin from dependency. Gradle doesn't expose the plugin itself, even with api().
		create("enterprise") {
			id = "com.gradle.enterprise"
			implementationClass = "com.gradle.enterprise.gradleplugin.GradleEnterprisePlugin"
			dependencies {
				implementation(libs.gradle.enterprise)
			}
		}

		create("moduleRoot") {
			id = "net.twisterrob.gradle.build.module.root"
			implementationClass = "net.twisterrob.gradle.build.RootModulePlugin"
		}
		create("moduleGradlePlugin") {
			id = "net.twisterrob.gradle.build.module.gradle-plugin"
			implementationClass = "net.twisterrob.gradle.build.GradlePluginModulePlugin"
		}
		create("moduleLibrary") {
			id = "net.twisterrob.gradle.build.module.library"
			implementationClass = "net.twisterrob.gradle.build.LibraryModulePlugin"
		}
		create("publishing") {
			id = "net.twisterrob.gradle.build.publish"
			implementationClass = "net.twisterrob.gradle.build.publishing.PublishingPlugin"
		}
	}
}

repositories {
	mavenCentral()
	gradlePluginPortal()
}

// Note on `plugins { }`: when the version is declared in the plugins block (`plugins { id(...) version "..." }`),
// the referenced dependencies are visible by IntelliJ Gradle Sync, but the breakpoints are not hit.
// Declaring all the dependencies in this project resolves this issue.
dependencies {
	implementation(libs.kotlin.gradle)
	implementation(libs.kotlin.dokka)
	implementation(libs.detekt)
	compileOnly(libs.nexus)

	// TODEL hack from https://github.com/gradle/gradle/issues/15383#issuecomment-779893192 (there are more parts to this)
	compileOnly(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}

// Reusing code from :plugin:settings in the main project.
run {
	// Rather than directly including the source folder, it's first copied here.
	val sharedCodeFolder: File = file("src/main/kotlin-shared")
	kotlin.sourceSets.named("main").configure { kotlin.srcDir(sharedCodeFolder) }
	// This is to make sure that IDEA doesn't mess things up with duplicated source roots.
	val copyReusableSources = tasks.register<Copy>("copyReusableSources") {
		// More robust: gradle.parent!!.rootProject.project(":plugin:settings").file("src/main/kotlin-shared")
		// but at this point in the lifecycle the parent (including build) rootProject build is not available yet.
		from(rootProject.file("../../plugin/settings/src/main/kotlin-shared"))
		into(sharedCodeFolder)
	}
	// The copied code is marked as generated for IDEA, so it warns when it's accidentally edited.
	idea.module.generatedSourceDirs.add(sharedCodeFolder)
	// Some tasks will rely on this copied code, so make sure their inputs are appropriately marked. 
	tasks.named("compileKotlin").configure { dependsOn(copyReusableSources) }
	tasks.named("detektMain").configure { dependsOn(copyReusableSources) }
	tasks.named("detekt").configure { dependsOn(copyReusableSources) }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
	kotlinOptions.verbose = true
	kotlinOptions.allWarningsAsErrors = true
}

// Note: duplicated from DetektPlugin because can't apply project this build.gradle.kts is defining.
detekt {
	// TODEL https://github.com/detekt/detekt/issues/4926
	buildUponDefaultConfig = false
	allRules = true
	ignoreFailures = true
	//debug = true
	config = project.rootProject.files("../../config/detekt/detekt.yml")
	baseline = project.rootProject.file("../../config/detekt/detekt-baseline-gradle-plugins.xml")
	basePath = project.rootProject.projectDir.resolve("../..").absolutePath

	parallel = true

	project.tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
		reports {
			html.required.set(true) // human
			txt.required.set(true) // console
		}
	}
}
