plugins {
//	kotlin("jvm")
	`java-gradle-plugin`
	`java-test-fixtures`
	id("net.twisterrob.gradle.build.publishing")
}

base.archivesName.set("twister-quality")
description = "Quality: All quality plugins bundled in one."

gradlePlugin {
	plugins {
		create("net.twisterrob.quality") {
			id = "net.twisterrob.quality"
			implementationClass = "net.twisterrob.gradle.quality.QualityPlugin"
		}
	}
}

dependencies {
	api(projects.common)
	api(projects.checkstyle)
	api(projects.pmd)

	implementation(projects.compat.gradle)
	implementation(projects.compat.agpBase)
	implementation(projects.compat.agp42x)
	implementation(projects.compat.agpLatest)
	implementation(projects.compat.agp)

	compileOnly(libs.annotations.jetbrains)
	compileOnly(libs.android.gradle) {
		configurations.compileOnly {
			// +--- com.android.tools.build:gradle:7.2.1
			//|    +--- com.android.tools:sdk-common:30.2.1
			//|    |    \--- xerces:xercesImpl:2.12.0
			//|    |         \--- xml-apis:xml-apis:1.4.01
			// Without this exclude, it brings in an incompatible version of javax.xml.stream.XMLOutputFactory
			// which doesn't have a "newFactory(String, ClassLoader)" method.
			// See net.twisterrob.gradle.quality.report.html.XMLStreamWriterDSLKt.bestXMLOutputFactory.
			exclude("xml-apis", "xml-apis") // 1.4.01
		}
	}
	// Need com.android.utils.FileUtils for HtmlReportTask.
	compileOnly(libs.android.tools.common)
//	compileOnly ("de.aaschmid:gradle-cpd-plugin:1.0")
	api(libs.violations)

	testImplementation(projects.test.internal)
	testRuntimeOnly(libs.android.gradle)

	testImplementation(testFixtures(projects.pmd))
	testImplementation(testFixtures(projects.checkstyle))

	testFixturesImplementation(projects.test.internal)
}

tasks.register("tests") {
	listOf(
		projects.quality,
		projects.common,
		projects.checkstyle,
		projects.pmd,
		projects.test,
		projects.test.internal,
	).forEach {
		dependsOn(it.dependencyProject.tasks.named("test"))
	}
}

pullTestResourcesFrom(projects.test)
