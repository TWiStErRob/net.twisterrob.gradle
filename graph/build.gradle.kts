import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.utils.extendsFrom
import java.text.SimpleDateFormat
import java.util.Date

plugins {
	id("java-gradle-plugin")
	id("org.openjfx.javafxplugin") version "0.0.14"
	id("org.jetbrains.kotlin.jvm") version "1.8.22"
	id("io.gitlab.arturbosch.detekt") version "1.23.0"
	id("org.gradle.idea")
}

group = "net.twisterrob.gradle"
version = "0.1"

gradlePlugin {
	plugins {
		register("graph") {
			id = "net.twisterrob.graph"
			implementationClass = "net.twisterrob.gradle.graph.GraphPlugin"
		}
	}
}

repositories {
	mavenCentral()
}

val webjars by configurations.registering {
	attributes {
		attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
	}
	isVisible = false
	isCanBeConsumed = false
	isCanBeResolved = true
}
// Expose it to the compiler, so it's visible as a dependency too.
configurations.compileOnly.extendsFrom(webjars)

dependencies {
	api(gradleApi()) // java-gradle-plugin
	implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
	implementation("org.jetbrains.kotlin:kotlin-stdlib")
	implementation("org.graphstream:gs-core:1.3")
//	implementation("org.graphstream:gs-core:2.0")
//	implementation("org.graphstream:gs-ui-swing:2.0")
	implementation("com.google.code.gson:gson:2.10.1")
	implementation("org.jetbrains:annotations:24.0.1")

	webjars.name("org.webjars:d3js:3.5.5")

	testImplementation("junit:junit:4.13.2")
}

javafx {
	modules = listOf(
		"javafx.controls", // implementation("org.openjfx:javafx-controls:17")
		"javafx.web", // implementation("org.openjfx:javafx-web:17")
		"javafx.swing", // implementation("org.openjfx:javafx-swing:17")
	)
}

java {
	sourceCompatibility = JavaVersion.VERSION_1_8
	targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<JavaCompile>().configureEach {
	options.compilerArgs = options.compilerArgs + listOf(
		"-Xlint:unchecked",
		"-Xlint:deprecation",
	)
}

tasks.withType<KotlinCompile>().configureEach {
	compilerOptions.jvmTarget.set(JvmTarget.fromTarget("1.8"))
	compilerOptions.allWarningsAsErrors.set(true)
}

tasks.named<Jar>("jar") {
	manifest {
		attributes(
			"Implementation-Vendor" to project.group,
			"Implementation-Title" to project.name,
			"Implementation-Version" to project.version,
			"Built-Date" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date()),
		)
	}
}

idea {
	module {
		fun excludedInProject(dir: File): List<File> =
			listOf(
				dir.resolve(".gradle"),
				dir.resolve("build"),
				dir.resolve(".idea"),
			)

		val examples = listOf("sample", "sample/app", "sample/lib")
			.map { rootDir.resolve(it) }
			.flatMap(::excludedInProject)

		excludeDirs.addAll(examples)
	}
}

detekt {
	buildUponDefaultConfig = false
	allRules = true
	config.setFrom(rootProject.file("../config/detekt/detekt.yml"))
	baseline = rootProject.file("../config/detekt/detekt-baseline-graph.xml")
	basePath = rootProject.projectDir.absolutePath
	parallel = true
}

val extractWebJars by tasks.registering(ExtractWebJarsTask::class) {
	configuration.setFrom(project.configurations["webjars"])
	artifacts.set(project.configurations["webjars"].incoming.artifactView { }.artifacts.resolvedArtifacts)
	outputDirectory.set(project.layout.dir(sourceSets.main.map { it.resources.srcDirs.first() }))
}
tasks.processResources.configure { dependsOn(extractWebJars) }

// Note: alternative config creating another resource folder. It works, but doesn't allow local testing. 
//ExtractWebJarsTask.this.cleanFirst.set(true)
//ExtractWebJarsTask.this.outputDirectory.set(project.layout.buildDirectory.dir("webjars-as-resources"))
//sourceSets.main.configure { resources.srcDir(extractWebJars) }

@UntrackedTask(because = "The output directory might overlap with existing source folder.")
abstract class ExtractWebJarsTask @Inject constructor(
	private val files: FileSystemOperations,
	private val archives: ArchiveOperations,
) : DefaultTask() {

	@get:CompileClasspath
	@get:SkipWhenEmpty
	abstract val configuration: ConfigurableFileCollection

	@get:Internal
	abstract val artifacts: SetProperty<ResolvedArtifactResult>

	@get:Input
	@get:Optional
	abstract val cleanFirst: Property<Boolean>

	@get:OutputDirectory
	abstract val outputDirectory: DirectoryProperty

	@TaskAction
	fun extract() {
		if (cleanFirst.getOrElse(false)) {
			files.delete { delete(outputDirectory) }
		}
		artifacts.get().forEach { artifact ->
			val id = artifact.id.componentIdentifier as? ModuleComponentIdentifier ?: return
			val localWebJar = artifact.file
			val name = id.module
			val version = id.version
			files.copy {
				duplicatesStrategy = DuplicatesStrategy.FAIL
				into(outputDirectory)
				from(archives.zipTree(localWebJar))
				include("META-INF/resources/webjars/${name}/${version}/*.js")
				exclude("META-INF/resources/webjars/${name}/${version}/webjars-requirejs.js")
				eachFile {
					// https://docs.gradle.org/current/userguide/working_with_files.html#ex-unpacking-a-subset-of-a-zip-file
					relativePath = RelativePath(true, relativePath.segments.last())
				}
				includeEmptyDirs = false
			}
		}
	}
}
