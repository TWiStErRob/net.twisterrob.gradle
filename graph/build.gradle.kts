import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("java-gradle-plugin")
	id("org.openjfx.javafxplugin") version "0.0.14"
	id("org.jetbrains.kotlin.jvm") version "1.9.23"
	id("io.gitlab.arturbosch.detekt") version "1.23.5"
	id("org.gradle.idea")
	id("net.twisterrob.gradle.build.webjars")
	id("org.jetbrains.kotlinx.kover") version "0.7.6"
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

dependencies {
	api(gradleApi()) // java-gradle-plugin
	implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
	implementation("org.jetbrains.kotlin:kotlin-stdlib")
	implementation("org.graphstream:gs-core:1.3") {
		exclude(group = "junit", module = "junit")
	}
	implementation("org.slf4j:slf4j-api:2.0.12")
//	implementation("org.graphstream:gs-core:2.0")
//	implementation("org.graphstream:gs-ui-swing:2.0")
	implementation("com.google.code.gson:gson:2.10.1")
	implementation("org.jetbrains:annotations:24.1.0")

	"webjars"("org.webjars.npm:d3:7.8.5") {
		// Avoid pulling in all small modules, using the merged .js file instead.
		isTransitive = false
	}

	testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
	testImplementation("org.junit.platform:junit-platform-launcher:1.10.2")
	testImplementation("org.mockito:mockito-core:5.11.0")
	testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
	testImplementation("org.hamcrest:hamcrest:2.2")
}

javafx {
	modules = listOf(
		"javafx.controls", // implementation("org.openjfx:javafx-controls:17")
		"javafx.web", // implementation("org.openjfx:javafx-web:17")
		"javafx.swing", // implementation("org.openjfx:javafx-swing:17")
	)
}

java {
	sourceCompatibility = JavaVersion.VERSION_11
	targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<JavaCompile>().configureEach {
	options.compilerArgs = options.compilerArgs + listOf(
		"-Xlint:unchecked",
		"-Xlint:deprecation",
	)
}

tasks.withType<KotlinCompile>().configureEach {
	compilerOptions.jvmTarget = JvmTarget.fromTarget("11")
	compilerOptions.allWarningsAsErrors = true
}

webjars {
	extractIntoFirstJavaResourcesFolder()
}

tasks.named<Jar>("jar") {
	manifest {
		attributes(
			"Implementation-Vendor" to project.group,
			"Implementation-Title" to project.name,
			"Implementation-Version" to project.version,
			//"Built-Date" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date()),
		)
	}
}

tasks.test.configure {
	useJUnitPlatform()
}

koverReport {
	defaults {
		verify {
			onCheck = false
		}
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
