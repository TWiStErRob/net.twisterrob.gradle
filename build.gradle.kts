import Libs.Kotlin.replaceKotlinJre7WithJdk7
import Libs.Kotlin.replaceKotlinJre8WithJdk8
import org.gradle.api.tasks.testing.TestOutputEvent.Destination
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.utils.keysToMap
import java.text.SimpleDateFormat
import java.util.Date
import java.util.EnumSet
import kotlin.math.absoluteValue

plugins {
//	kotlin("jvm") apply false
	id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}

val projectVersion: String by project

description = "Quality plugin for Gradle that supports Android flavors."
allprojects {
	group = rootProject.name
	version = projectVersion
}

subprojects {
	apply { plugin("kotlin") }

	repositories {
		mavenCentral()
		google()
		// for Kotlin-DSL
		maven { setUrl("https://repo.gradle.org/gradle/libs-releases-local/") }
	}
}

allprojects {
	replaceGradlePluginAutoDependenciesWithoutKotlin()

	configurations.all {
		replaceKotlinJre7WithJdk7()
		replaceKotlinJre8WithJdk8()
		resolutionStrategy {
			// make sure we don't have many versions of Kotlin lying around
			force(Libs.Kotlin.stdlib)
			force(Libs.Kotlin.reflect)
			@Suppress("DEPRECATION") // force version so that it's upgraded correctly with useTarget
			force(Libs.Kotlin.stdlibJre7)
			force(Libs.Kotlin.stdlibJdk7)
			@Suppress("DEPRECATION") // force version so that it's upgraded correctly with useTarget
			force(Libs.Kotlin.stdlibJre8)
			force(Libs.Kotlin.stdlibJdk8)
		}
	}

	gradle.projectsEvaluated {
		tasks.withType<JavaCompile> {
			options.compilerArgs.addAll(
				listOf(
					"-Werror", // fail on warnings
					"-Xlint:all", // enable all possible checks
					"-Xlint:-processing" // except "No processor claimed any of these annotations"
				)
			)
		}
		tasks.withType<GroovyCompile> {
			options.compilerArgs.addAll(
				listOf(
					"-Werror", // fail on warnings
					"-Xlint:all" // enable all possible checks
				)
			)
			groovyOptions.configurationScript = rootProject.file("gradle/compileGroovy.groovy")
			// enable Java 7 invokeDynamic, since Java target is > 7 (Android requires Java 8 at least)
			// no need for groovy-all:ver-indy, because the classpath is provided from hosting Gradle project
			groovyOptions.optimizationOptions!!["indy"] = true
		}
		tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
			kotlinOptions.verbose = true
			kotlinOptions.jvmTarget = Libs.javaVersion.toString()
			kotlinOptions.allWarningsAsErrors = true
		}

		tasks.withType<Test> {
			useJUnitPlatform()

			if (System.getProperties().containsKey("idea.paths.selector")) {
				logger.debug("Keeping folder contents after test run from IDEA")
				// see net.twisterrob.gradle.test.GradleRunnerRule
				jvmArgs("-Dnet.twisterrob.gradle.runner.clearAfterSuccess=false")
				jvmArgs("-Dnet.twisterrob.gradle.runner.clearAfterFailure=false")
			}
			val propertyNamesToExposeToJUnitTests = listOf(
				// for GradleRunnerRule to use a different Gradle version for tests
				"net.twisterrob.gradle.runner.gradleVersion",
				// for tests to decide dynamically
				"net.twisterrob.test.android.pluginVersion",
				"net.twisterrob.test.android.compileSdkVersion"
			)
			val properties = propertyNamesToExposeToJUnitTests.keysToMap { project.findProperty(it) }
			properties.forEach { (name, value) -> inputs.property(name, value) }
			properties.forEach { (name, value) -> value?.let { jvmArgs("-D${name}=${value}") } }
		}

		tasks.withType<ProcessResources> {
			val propertyNamesToReplace = listOf(
				"net.twisterrob.test.android.pluginVersion",
				"net.twisterrob.test.android.compileSdkVersion"
			)
			val properties = propertyNamesToReplace.keysToMap { project.findProperty(it) }
			properties.forEach { (name, value) -> inputs.property(name, value) }
			filesMatching(listOf("**/build.gradle", "**/settings.gradle")) {
				val replacements = properties + mapOf(
					// custom replacements (`"name" to value`) would come here
				)
				filter(mapOf("tokens" to replacements), org.apache.tools.ant.filters.ReplaceTokens::class.java)
			}
		}
	}

	plugins.withId("kotlin") {
		dependencies {
			//add("implementation", "org.funktionale:funktionale-partials:1.2")
			add("compileOnly", Libs.Kotlin.dsl) {
				isTransitive = false // make sure to not pull in kotlin-compiler-embeddable
			}
			add("api", Libs.Kotlin.stdlib)
			add("api", Libs.Kotlin.stdlibJdk8)
			add("api", Libs.Kotlin.reflect)

			add("testImplementation", Libs.Kotlin.test)
		}
	}

	plugins.withId("java") {
		val java = convention.getPluginByName<JavaPluginConvention>("java")
		java.sourceCompatibility = Libs.javaVersion
		java.targetCompatibility = Libs.javaVersion
		(tasks["test"] as Test).testLogging.events("passed", "skipped", "failed")
		afterEvaluate {
			with(tasks["jar"] as Jar) {
				manifest {
					attributes(
						mapOf(
							// Implementation-* used by TestPlugin
							"Implementation-Vendor" to project.group,
							"Implementation-Title" to project.base.archivesBaseName,
							"Implementation-Version" to project.version,
							// TODO Make sure it doesn't change often (skip for SNAPSHOT)
							// otherwise :jar always re-packages and compilations cascade
							"Built-Date" to SimpleDateFormat("yyyy-MM-dd'T'00:00:00Z").format(Date())
						)
					)
				}
			}
		}
	}

	if (project.property("net.twisterrob.gradle.build.verboseReports").toString().toBoolean()) {
		tasks.withType<Test> {
			testLogging {
				// disable all events, output handled by custom callbacks below
				events = EnumSet.noneOf(TestLogEvent::class.java)
				//events = TestLogEvent.values().toSet() - TestLogEvent.STARTED
				exceptionFormat = TestExceptionFormat.FULL
				showExceptions = true
				showCauses = true
				showStackTraces = true
			}
			class TestInfo(
				val descriptor: TestDescriptor,
				val stdOut: StringBuilder = StringBuilder(),
				val stdErr: StringBuilder = StringBuilder()
			)

			val lookup = mutableMapOf<TestDescriptor, TestInfo>()
			beforeTest(KotlinClosure1<TestDescriptor, Any>({
				lookup.put(this, TestInfo(this))
			}))
			onOutput(KotlinClosure2({ descriptor: TestDescriptor, event: TestOutputEvent ->
				val info = lookup.getValue(descriptor)
				when (event.destination!!) {
					Destination.StdOut -> info.stdOut.append(event.message)
					Destination.StdErr -> info.stdErr.append(event.message)
				}
			}))
			afterTest(KotlinClosure2({ descriptor: TestDescriptor, result: TestResult ->
				val info = lookup.remove(descriptor)!!
				fun fold(type: String, condition: Boolean, output: () -> Unit) {
					val id = descriptor.toString().hashCode().absoluteValue
					if (condition) {
						println("::group::test_${type}_${id}")
						output()
						println("::endgroup:: ")
					}
				}
				println("${descriptor.className} > ${descriptor.name} ${result.resultType}")
				fold("ex", result.exception != null) {
					result.exception!!.printStackTrace()
				}
				fold("out", info.stdOut.isNotEmpty()) {
					println("STANDARD_OUT")
					println(info.stdOut)
				}
				fold("err", info.stdErr.isNotEmpty()) {
					println("STANDARD_ERR")
					println(info.stdErr)
				}
			}))
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

nexusPublishing {
	repositories {
		@Suppress("UnstableApiUsage")
		sonatype {
			// For :publishReleasePublicationToSonatypeRepository, projectVersion suffix chooses repo.
			nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
			snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))

			// For :closeAndReleaseSonatypeStagingRepository
			// Set via -PsonatypeStagingProfileId to gradlew, or ORG_GRADLE_PROJECT_sonatypeStagingProfileId env var.
			val sonatypeStagingProfileId: String? by project
			stagingProfileId.set(sonatypeStagingProfileId)

			// For everything sonatype, but automatically done by the plugin.
			// Set via -PsonatypeUsername to gradlew, or ORG_GRADLE_PROJECT_sonatypeUsername env var.
			//username.set(val sonatypeUsername: String? by project)
			// Set via -PsonatypePassword to gradlew, or ORG_GRADLE_PROJECT_sonatypePassword env var.
			//password.set(val sonatypePassword: String? by project)
		}
	}
}
