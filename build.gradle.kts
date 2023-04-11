import net.twisterrob.gradle.doNotNagAbout
import java.time.Instant
import java.time.format.DateTimeFormatter

plugins {
	@Suppress("DSL_SCOPE_VIOLATION") // TODEL https://github.com/gradle/gradle/issues/22797
	alias(libs.plugins.nexus)
	id("net.twisterrob.gradle.build.module.root")
	id("org.gradle.idea")
}

description = "Plugins for Gradle that support Android flavors."

buildscript { enableDependencyLocking(project) }
allprojects { enableDependencyLocking() }

subprojects {
	// Extension with name 'libs' does not exist. Currently registered extension names: [ext, kotlin, kotlinTestRegistry, base, defaultArtifacts, sourceSets, reporting, java, javaToolchains, testing]
	// Needs to be called different from libs,
	// because com.android.tools.idea.gradle.dsl.model.ext.PropertyUtil.followElement
	// from idea-2021.1.3\plugins\android-gradle-dsl\lib\android-gradle-dsl-impl.jar
	// runs into an infinite loop on it.
	// TODEL Affects anything with Android Plugin < 2020.3.1 (i.e. AS 4.x, and IJ <2021.3)
	val deps = rootProject.libs
	replaceGradlePluginAutoDependenciesWithoutKotlin()

	configurations.all {
		replaceHamcrestDependencies(project)
	}

	plugins.withId("org.jetbrains.kotlin.jvm") {
		dependencies {
			// Make sure we don't have many versions of Kotlin lying around.
			add("compileOnly", enforcedPlatform(deps.kotlin.bom))
			add("testCompileOnly", enforcedPlatform(deps.kotlin.bom))
			plugins.withId("org.gradle.java-test-fixtures") {
				add("testFixturesCompileOnly", enforcedPlatform(deps.kotlin.bom))
			}

			add("compileOnly", deps.kotlin.dsl) {
				isTransitive = false // make sure to not pull in kotlin-compiler-embeddable
			}
			add("api", deps.kotlin.stdlib)
			add("api", deps.kotlin.stdlib.jdk8)
			add("api", deps.kotlin.reflect)
		}
	}

	plugins.withId("org.gradle.java") {
		afterEvaluate {
			// Delayed configuration, so that project.* is set up properly in corresponding modules' build.gradle.kts.
			tasks.named<Jar>("jar") {
				manifest {
					attributes(
						mapOf(
							// Implementation-* used by TestPlugin
							"Implementation-Vendor" to project.group,
							"Implementation-Title" to project.base.archivesName.get(),
							"Implementation-Version" to project.version,
							"Built-Date" to DateTimeFormatter.ISO_INSTANT.format(Instant.now())
						)
					)
				}
			}
		}
	}

	normalization {
		runtimeClasspath {
			metaInf {
				ignoreAttribute("Built-Date")
			}
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

tasks.register("check") {
	description = "Delegate task for checking included builds too."
	dependsOn(gradle.includedBuild("plugins").task(":check"))
}

project.tasks.register<TestReport>("testReport") {
	group = LifecycleBasePlugin.VERIFICATION_GROUP
	description = "Run and report on all tests in the project. Add `-x test` to just generate report."
	destinationDirectory.set(file("${buildDir}/reports/tests/all"))

	val tests = subprojects
		.flatMap { it.tasks.withType(Test::class) } // Forces to create the tasks.
		.onEach { it.ignoreFailures = true } // Let the tests finish, to get a final "all" report.
	// Detach (.get()) the result directories,
	// simply using reportOn(tests) or the binaryResultsDirectory providers, task dependencies would be created.
	testResults.from(tests.map { it.binaryResultsDirectory.get() })
	// Force executing tests (if they're in the task graph), before reporting on them.
	mustRunAfter(tests)

	doLast {
		val reportFile = destinationDirectory.file("index.html").get().asFile
		val failureRegex = Regex("""(?s).*<div class="infoBox" id="failures">\s*<div class="counter">(\d+)<\/div>.*""")
		val failureMatch = failureRegex.matchEntire(reportFile.readText())
		val reportPath = reportFile.toURI().toString().replace("file:/([A-Z])".toRegex(), "file:///\$1")
		if (failureMatch == null) {
			throw GradleException("Cannot determine if the tests failed. See the report at: ${reportPath}")
		} else {
			val failCount = failureMatch.groups[1]!!.value
			if (failCount != "0") {
				throw GradleException("There were ${failCount} failing tests. See the report at: ${reportPath}")
			}
		}
	}
}

// To get gradle/dependency-locks run `gradlew :allDependencies --write-locks`.
project.tasks.register<Task>("allDependencies") {
	val projects = project.allprojects.sortedBy { it.name }
	doFirst {
		println(projects.joinToString(prefix = "Printing dependencies for modules:\n", separator = "\n") { " * ${it}" })
	}
	val dependenciesTasks = projects.map { it.tasks.named("dependencies") }
	// Builds a dependency chain: 1 <- 2 <- 3 <- 4, so when executed they're in order.
	dependenciesTasks.reduce { acc, task -> task.apply { get().dependsOn(acc) } }
	// Use finalizedBy instead of dependsOn to make sure this task executes first.
	this@register.finalizedBy(dependenciesTasks)
}

project.tasks.register<Delete>("cleanDebug") {
	group = LifecycleBasePlugin.VERIFICATION_GROUP
	description = "Clean outputs generated by debug projects."
	// Hacky version of the following with intuitive results (i.e. the folders are also deleted):
	// ```
	// delete(fileTree(rootProject.file("docs/debug")) {
	//     include("*/.gradle")
	//     include("*/build")
	//     include("*/buildSrc/.gradle")
	//     include("*/buildSrc/build")
	// })
	// ```
	// See https://github.com/gradle/gradle/issues/14152#issuecomment-953610543.
	fileTree(rootProject.file("docs/debug")) {
		include("*/.gradle")
		include("*/buildSrc/.gradle")
	}.visit { if (name == ".gradle") delete(file) }
	fileTree(rootProject.file("docs/debug")) {
		include("*/build")
		include("*/buildSrc/build")
	}.visit { if (name == "build") delete(file) }
}

nexusPublishing {
	repositories {
		sonatype {
			// For :publish...PublicationToSonatypeRepository, projectVersion suffix chooses repo.
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

idea {
	module {
		fun excludedInProject(dir: File): List<File> =
			listOf(
				dir.resolve(".gradle"),
				dir.resolve("build"),
				dir.resolve("buildSrc/.gradle"),
				dir.resolve("buildSrc/build"),
				dir.resolve(".idea"),
			)

		val examples = listOf("local", "release", "snapshot")
			.map { rootDir.resolve("docs/examples").resolve(it) }
			.flatMap(::excludedInProject)
		val debuggers = rootDir
			.resolve("docs/debug")
			.listFiles { file: File -> file.isDirectory }
			.flatMap(::excludedInProject)
		val unpackagedResources = allprojects.map { it.projectDir.resolve("build/unPackagedTestResources") }
		excludeDirs.addAll(examples + debuggers + unpackagedResources)
	}
}

val gradleVersion: String = GradleVersion.current().version

// TODEL Gradle sync in IDEA 2022.3.1: https://youtrack.jetbrains.com/issue/IDEA-306975
@Suppress("MaxLineLength")
doNotNagAbout(
	"The AbstractArchiveTask.archivePath property has been deprecated. " +
			"This is scheduled to be removed in Gradle 9.0. " +
			"Please use the archiveFile property instead. " +
			"See https://docs.gradle.org/${gradleVersion}/dsl/org.gradle.api.tasks.bundling.AbstractArchiveTask.html#org.gradle.api.tasks.bundling.AbstractArchiveTask:archivePath for more details.",
	"at org.jetbrains.plugins.gradle.tooling.builder.ExternalProjectBuilderImpl\$_getSourceSets_closure"
)
// TODEL Gradle sync in IDEA 2022.3.1: https://youtrack.jetbrains.com/issue/IDEA-306975
@Suppress("MaxLineLength")
doNotNagAbout(
	"The AbstractArchiveTask.archivePath property has been deprecated. " +
			"This is scheduled to be removed in Gradle 9.0. " +
			"Please use the archiveFile property instead. " +
			"See https://docs.gradle.org/${gradleVersion}/dsl/org.gradle.api.tasks.bundling.AbstractArchiveTask.html#org.gradle.api.tasks.bundling.AbstractArchiveTask:archivePath for more details.",
	"at org.jetbrains.plugins.gradle.tooling.util.SourceSetCachedFinder.createArtifactsMap"
)
