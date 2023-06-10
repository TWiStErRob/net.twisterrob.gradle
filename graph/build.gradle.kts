import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.text.SimpleDateFormat
import java.util.Date

plugins {
	id("java-gradle-plugin")
	id("org.openjfx.javafxplugin") version "0.0.14"
	id("org.jetbrains.kotlin.jvm") version "1.8.22"
	id("io.gitlab.arturbosch.detekt") version "1.23.0"
	id("org.gradle.idea")
}
apply(from = "gradle/webjars.gradle.kts")

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

dependencies {
	api(gradleApi()) // java-gradle-plugin
	implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
	implementation("org.jetbrains.kotlin:kotlin-stdlib")
	implementation("org.graphstream:gs-core:1.3")
//	implementation("org.graphstream:gs-core:2.0")
//	implementation("org.graphstream:gs-ui-swing:2.0")
	implementation("com.google.code.gson:gson:2.10.1")
	implementation("org.jetbrains:annotations:24.0.1")

	"webjars"("org.webjars:d3js:6.7.0")

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
