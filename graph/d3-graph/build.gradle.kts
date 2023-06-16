plugins {
	id("org.jetbrains.kotlin.multiplatform")
}

repositories {
	mavenCentral()
}

kotlin {
	js(IR) {
		moduleName = "d3-graph"
		browser()
		binaries.executable()
		//generateTypeScriptDefinitions()
	}
	@Suppress("UNUSED_VARIABLE")
	sourceSets {
		val jsMain by getting {
			languageSettings.optIn("kotlin.js.ExperimentalJsExport")
			dependencies {
				implementation("org.jetbrains.kotlin:kotlin-stdlib-js")
				implementation(npm("d3", "7.8.4"))
			}
		}
		val jsTest by getting {
			languageSettings.optIn("kotlin.js.ExperimentalJsExport")
			dependencies {
				implementation("org.jetbrains.kotlin:kotlin-test-js")
			}
		}
	}
}

//<editor-fold desc="Expose distribution folder">
val distributions: NamedDomainObjectProvider<Configuration> by configurations.registering {
	isCanBeConsumed = true
	isCanBeResolved = false
}

artifacts {
	add(distributions.name, tasks.named("jsBrowserDevelopmentExecutableDistribution"))
}
//</editor-fold>

// TODEL Workaround for https://youtrack.jetbrains.com/issue/KT-57203, fixed in 1.9.0-Beta.
tasks.named("jsBrowserProductionWebpack").configure {
	dependsOn("jsProductionExecutableCompileSync")
	dependsOn("jsDevelopmentExecutableCompileSync")
}
tasks.named("jsBrowserDevelopmentWebpack").configure {
	dependsOn("jsDevelopmentExecutableCompileSync")
	dependsOn("jsProductionExecutableCompileSync")
}
