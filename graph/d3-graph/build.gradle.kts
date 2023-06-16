plugins {
	id("org.jetbrains.kotlin.js")
	id("net.twisterrob.gradle.build.webjars")
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.jetbrains.kotlin:kotlin-stdlib-js")

	testImplementation("org.jetbrains.kotlin:kotlin-test-js")

	"webjars"("org.webjars.npm:d3:7.8.4") {
		// Avoid pulling in all small modules, using the merged .js file instead.
		isTransitive = false
	}
}

webjars {
	extractInto(kotlin.sourceSets["main"].resources)
}

kotlin {
	js(IR) {
		browser()
		binaries.executable()
		//generateTypeScriptDefinitions()
	}
	sourceSets.named("main") {
		languageSettings.optIn("kotlin.js.ExperimentalJsExport")
	}
}

//<editor-fold desc="Expose distribution folder">
val distributions: NamedDomainObjectProvider<Configuration> by configurations.registering {
	isCanBeConsumed = true
	isCanBeResolved = false
}

artifacts {
	add(distributions.name, tasks.named("browserDistribution"))
}
//</editor-fold>
