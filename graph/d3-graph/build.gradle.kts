plugins {
	id("org.jetbrains.kotlin.js")
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.jetbrains.kotlin:kotlin-stdlib-js")
	implementation(npm("d3", "7.8.4"))

	testImplementation("org.jetbrains.kotlin:kotlin-test-js")
}

kotlin {
	js(IR) {
		moduleName = "d3-graph"
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
	add(distributions.name, tasks.named("browserDevelopmentExecutableDistribution"))
}
//</editor-fold>

// TODEL Workaround for https://youtrack.jetbrains.com/issue/KT-57203, fixed in 1.9.0-Beta.
tasks.named("browserProductionWebpack").configure {
	dependsOn("productionExecutableCompileSync")
	dependsOn("developmentExecutableCompileSync")
}
tasks.named("browserDevelopmentWebpack").configure {
	dependsOn("developmentExecutableCompileSync")
	dependsOn("productionExecutableCompileSync")
}
