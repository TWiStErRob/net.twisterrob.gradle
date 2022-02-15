import org.jetbrains.kotlin.utils.keysToMap
import java.time.Instant
import java.time.format.DateTimeFormatter

plugins {
//	kotlin("jvm") apply false
	@Suppress("DSL_SCOPE_VIOLATION", "UnstableApiUsage")
	alias(libs.plugins.nexus)
}

val projectVersion: String by project

description = "Plugins for Gradle that support Android flavors."
allprojects {
	group = "net.twisterrob.gradle"
	version = projectVersion
}

resetGradleTestWorkerIdToDefault()

subprojects {
	apply { plugin("kotlin") }

	repositories {
		google()
		mavenCentral()
		// for Kotlin-DSL
		maven { setUrl("https://repo.gradle.org/gradle/libs-releases-local/") }
	}
}

allprojects {
	// Extension with name 'libs' does not exist. Currently registered extension names: [ext, kotlin, kotlinTestRegistry, base, defaultArtifacts, sourceSets, reporting, java, javaToolchains, testing]
	// Needs to be called different from libs,
	// because com.android.tools.idea.gradle.dsl.model.ext.PropertyUtil.followElement
	// from idea-2021.1.3\plugins\android-gradle-dsl\lib\android-gradle-dsl-impl.jar
	// runs into an infinite loop on it.
	// TODEL Affects anything with Android Plugin < 2020.3.1 (i.e. AS 4.x, and IJ <2021.3)
	val deps = rootProject.libs
	replaceGradlePluginAutoDependenciesWithoutKotlin()

	configurations.all {
		replaceKotlinJre7WithJdk7()
		replaceKotlinJre8WithJdk8()
		replaceHamcrestDependencies(project)
		resolutionStrategy {
			// Make sure we don't have many versions of Kotlin lying around.
			force(deps.kotlin.stdlib)
			force(deps.kotlin.reflect)
			// Force version so that it's upgraded correctly with useTarget.
			force(deps.kotlin.stdlib.jre7)
			force(deps.kotlin.stdlib.jdk7)
			// Force version so that it's upgraded correctly with useTarget.
			force(deps.kotlin.stdlib.jre8)
			force(deps.kotlin.stdlib.jdk8)
		}
	}

	gradle.projectsEvaluated {
		tasks.withType<JavaCompile>().configureEach {
			options.compilerArgs.addAll(
				listOf(
					"-Werror", // fail on warnings
					"-Xlint:all", // enable all possible checks
					"-Xlint:-processing" // except "No processor claimed any of these annotations"
				)
			)
		}
		tasks.withType<GroovyCompile>().configureEach {
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
		tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
			kotlinOptions.verbose = true
			kotlinOptions.jvmTarget = deps.versions.java.get()
			kotlinOptions.suppressWarnings = false
			kotlinOptions.allWarningsAsErrors = true
			kotlinOptions.freeCompilerArgs += listOf(
				// Caused by: java.lang.NoSuchMethodError: kotlin.jvm.internal.FunctionReferenceImpl.<init>(ILjava/lang/Object;Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;I)V
				//	at net.twisterrob.gradle.common.BaseQualityPlugin$apply$1$1.<init>(BaseQualityPlugin.kt)
				//	at net.twisterrob.gradle.common.BaseQualityPlugin$apply$1.execute(BaseQualityPlugin.kt:24)
				//	at net.twisterrob.gradle.common.BaseQualityPlugin$apply$1.execute(BaseQualityPlugin.kt:8)
				// https://youtrack.jetbrains.com/issue/KT-41852#focus=Comments-27-4604992.0-0
				"-Xno-optimized-callable-references"
			)
		}

		tasks.withType<Test>().configureEach {
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

		tasks.withType<@Suppress("UnstableApiUsage") ProcessResources>().configureEach {
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
			add("compileOnly", deps.kotlin.dsl) {
				isTransitive = false // make sure to not pull in kotlin-compiler-embeddable
			}
			add("api", deps.kotlin.stdlib)
			add("api", deps.kotlin.stdlib.jdk8)
			add("api", deps.kotlin.reflect)

			add("testImplementation", deps.kotlin.test)
		}
	}

	plugins.withId("java") {
		val java = extensions.getByName<JavaPluginExtension>("java")
		java.sourceCompatibility = JavaVersion.toVersion(deps.versions.java.get())
		java.targetCompatibility = JavaVersion.toVersion(deps.versions.java.get())
		tasks.named<Test>("test") { testLogging.events("passed", "skipped", "failed") }
		afterEvaluate {
			tasks.named<Jar>("jar") {
				manifest {
					attributes(
						mapOf(
							// Implementation-* used by TestPlugin
							"Implementation-Vendor" to project.group,
							"Implementation-Title" to project.base.archivesName.get(),
							"Implementation-Version" to project.version,
							"Built-Date" to if (projectVersion.endsWith("-SNAPSHOT"))
								DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(0))
							else
								DateTimeFormatter.ISO_INSTANT.format(Instant.now())
						)
					)
				}
			}
		}
	}

	if (project.property("net.twisterrob.gradle.build.verboseReports").toString().toBoolean()) {
		tasks.withType<Test>().configureEach {
			configureVerboseReportsForGithubActions()
		}
	}
}
if (project.property("net.twisterrob.gradle.build.includeExamples").toString().toBoolean()) {
	tasks.register("assembleExamples") {
		dependsOn(gradle.includedBuilds.map { it.task(":assemble") })
	}
	tasks.register("checkExamples") {
		dependsOn(gradle.includedBuilds.map { it.task(":check") })
	}
}

project.tasks.register<TestReport>("testReport") {
	group = LifecycleBasePlugin.VERIFICATION_GROUP
	description = "Run and report on all tests in the project. Add -x test to just generate report."
	destinationDir = file("${buildDir}/reports/tests/all")

	val tests = subprojects
		.flatMap { it.tasks.withType(Test::class) } // Forces to create the tasks.
		.onEach { it.ignoreFailures = true } // Let the tests finish, to get a final "all" report.
	// Detach the result directories, simply using reportOn(tests) or the providers, task dependencies will be created.
	reportOn(tests.map { it.binaryResultsDirectory.get() })
	// Force executing tests (if they're in the task graph), before reporting on them.
	mustRunAfter(tests)

	doLast {
		val reportFile = destinationDir.resolve("index.html")
		val successRegex = """(?s)<div class="infoBox" id="failures">\s*<div class="counter">0<\/div>""".toRegex()
		if (!successRegex.containsMatchIn(reportFile.readText())) {
			val reportPath = reportFile.toURI().toString().replace("file:/([A-Z])".toRegex(), "file:///\$1")
			throw GradleException("There were failing tests. See the report at: ${reportPath}")
		}
	}
}

nexusPublishing {
	repositories {
		sonatype {
			// For :publishReleasePublicationToSonatypeRepository, projectVersion suffix chooses repo.
			nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
			snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))

			// For :closeAndReleaseSonatypeStagingRepository
			// Set via -PsonatypeStagingProfileId to gradlew, or ORG_GRADLE_PROJECT_sonatypeStagingProfileId env var.
			val sonatypeStagingProfileId: String? by project
			stagingProfileId.set(sonatypeStagingProfileId)

			val sonatypeUsername: String? by project
			val sonatypePassword: String? by project
			require((sonatypeUsername == null) == (sonatypePassword == null)) {
				// Explicit check for existence of both, because otherwise it just fails with a misleading error:
				// > Execution failed for task ':initializeSonatypeStagingRepository'.
				// > > Failed to load staging profiles, server at ${nexusUrl} responded with status code 401, body:
				"Missing username (${sonatypeUsername == null}) or password (${sonatypePassword == null})."
			}
			// For everything sonatype, but automatically done by the plugin.
			// Set via -PsonatypeUsername to gradlew, or ORG_GRADLE_PROJECT_sonatypeUsername env var.
			//username.set(sonatypeUsername)
			// Set via -PsonatypePassword to gradlew, or ORG_GRADLE_PROJECT_sonatypePassword env var.
			//password.set(sonatypePassword)
		}
	}
}
