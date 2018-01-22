import org.gradle.api.tasks.bundling.Jar
import java.io.File
import java.util.Date
import java.text.SimpleDateFormat

plugins {
	`base` // just to get some support for subproject stuff, for example access to project.base
}

val VERSION by project
val VERSION_JAVA by project
val VERSION_KOTLIN by project
val VERSION_KOTLIN_DSL by project

buildscript {
	repositories {
		jcenter()
	}

	dependencies {
		// TODO https://github.com/gradle/kotlin-dsl/issues/535
		classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.2.20")
//		classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${VERSION_KOTLIN}")
	}
}

group = rootProject.name

subprojects {
	group = rootProject.group
	version = VERSION!!

	repositories {
		jcenter()
		google()
		// for Kotlin-DSL
		maven { setUrl("https://repo.gradle.org/gradle/libs-releases-local/") }
	}

	if (System.getenv("MVN_LOCALHOST_REPO") != null) {
		apply { from("http://localhost/maven/configure.gradle") }
	}
}

allprojects {

	configurations.all {
		resolutionStrategy {
			// make sure we don't have many versions of Kotlin lying around
			force("org.jetbrains.kotlin:kotlin-stdlib:${VERSION_KOTLIN}")
			force("org.jetbrains.kotlin:kotlin-reflect:${VERSION_KOTLIN}")
			force("org.jetbrains.kotlin:kotlin-stdlib-jre7:${VERSION_KOTLIN}")
			force("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${VERSION_KOTLIN}")
			force("org.jetbrains.kotlin:kotlin-stdlib-jre8:${VERSION_KOTLIN}")
			force("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${VERSION_KOTLIN}")
		}
	}

	gradle.projectsEvaluated {
		tasks.withType<JavaCompile> {
			options.compilerArgs.addAll(listOf("-Xlint:all", "-Werror"))
		}
		tasks.withType<GroovyCompile> {
			options.compilerArgs.addAll(listOf("-Xlint:all", "-Werror"))
			groovyOptions.configurationScript = rootProject.file("gradle/compileGroovy.groovy")
			// enable Java 7 invokeDynamic, since Java target is > 7 (Android requires Java 8 at least)
			// no need for groovy-all:ver-indy, because the classpath is provided from hosting Gradle project
			groovyOptions.optimizationOptions["indy"] = true
		}
		tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
			kotlinOptions.verbose = true
			kotlinOptions.jvmTarget = JavaVersion.toVersion(VERSION_JAVA!!).toString()
//			kotlinOptions.allWarningsAsErrors = true
		}
		tasks.withType<Test> {
			if (System.getProperties().containsKey("idea.paths.selector")) {
				logger.debug("Keeping folder contents after failed test running from IDEA")
				// see net.twisterrob.gradle.test.GradleRunnerRule
				jvmArgs("-Dnet.twisterrob.gradle.runner.clearAfterFailure=false")
			}
		}
	}

	plugins.withId("kotlin") {
		dependencies {
			//add("implementation", "org.funktionale:funktionale-partials:1.2")
			add("compileOnly", "org.gradle:gradle-kotlin-dsl:${VERSION_KOTLIN_DSL}")
			add("implementation", "org.jetbrains.kotlin:kotlin-stdlib:${VERSION_KOTLIN}")
			add("implementation", "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${VERSION_KOTLIN}")
			add("implementation", "org.jetbrains.kotlin:kotlin-reflect:${VERSION_KOTLIN}")

			add("testImplementation", "org.jetbrains.kotlin:kotlin-test:${VERSION_KOTLIN}")
			add("testImplementation", "org.jetbrains.kotlin:kotlin-test-junit:${VERSION_KOTLIN}")
		}
	}

	plugins.withId("java") {
		val java = convention.getPluginByName<JavaPluginConvention>("java")
		java.sourceCompatibility = JavaVersion.toVersion(VERSION_JAVA!!)
		java.targetCompatibility = JavaVersion.toVersion(VERSION_JAVA!!)
		(tasks["test"] as Test).testLogging.events("passed", "skipped", "failed")
		afterEvaluate {
			with(tasks["jar"] as Jar) {
				manifest {
					attributes(mapOf(
							// Implementation-* used by TestPlugin
							"Implementation-Vendor" to project.group,
							"Implementation-Title" to project.base.archivesBaseName,
							"Implementation-Version" to project.version,
							// TODO Make sure it doesn't change often (skip for SNAPSHOT)
							// otherwise :jar always re-packages and compilations cascade
							"Built-Date" to SimpleDateFormat("yyyy-MM-dd'T'00:00:00Z").format(Date())
					))
				}
			}
		}
	}
}

project.tasks.create("tests", TestReport::class.java) {
	destinationDir = file("${buildDir}/reports/tests/all")
	project.evaluationDependsOnChildren()
	allprojects.forEach { subproject ->
		subproject.tasks.withType<Test> {
			ignoreFailures = true
			reports.junitXml.isEnabled = true
			this@create.reportOn(this@withType)
		}
	}
	doLast {
		val reportFile = File(destinationDir, "index.html")
		val successRegex = """(?s)<div class="infoBox" id="failures">\s*<div class="counter">0<\/div>""".toRegex()
		if (!successRegex.containsMatchIn(reportFile.readText())) {
			throw GradleException("There were failing tests. See the report at: ${reportFile.toURI()}")
		}
	}
}
