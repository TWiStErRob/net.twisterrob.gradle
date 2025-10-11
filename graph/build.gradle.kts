import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	id("org.gradle.java-gradle-plugin")
	id("org.gradle.maven-publish")
	id("org.openjfx.javafxplugin") version "0.0.14"
	id("org.jetbrains.kotlin.jvm") version "2.2.20"
	id("io.gitlab.arturbosch.detekt") version "1.23.8"
	id("org.gradle.idea")
	id("net.twisterrob.gradle.build.webjars")
	id("org.jetbrains.kotlinx.kover") version "0.9.2"
}

group = "net.twisterrob.gradle"
version = "0.1-SNAPSHOT"

gradlePlugin {
	@Suppress("detekt.StringLiteralDuplication")
	plugins {
		register("graph") {
			id = "net.twisterrob.gradle.graph"
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
	implementation("org.slf4j:slf4j-api:2.0.17")
//	implementation("org.graphstream:gs-core:2.0")
//	implementation("org.graphstream:gs-ui-swing:2.0")
	implementation("com.google.code.gson:gson:2.13.2")
	implementation("org.jetbrains:annotations:26.0.2-1")

	"webjars"("org.webjars.npm:d3:7.9.0") {
		// Avoid pulling in all small modules, using the merged .js file instead.
		isTransitive = false
	}

	testImplementation("org.junit.jupiter:junit-jupiter:6.0.0")
	testImplementation("org.junit.platform:junit-platform-launcher")
	testImplementation("org.mockito:mockito-core:5.20.0")
	testImplementation("org.mockito.kotlin:mockito-kotlin:6.1.0")
	testImplementation("org.hamcrest:hamcrest:3.0")
}

javafx {
	modules = listOf(
		"javafx.controls", // implementation("org.openjfx:javafx-controls:17")
		"javafx.web", // implementation("org.openjfx:javafx-web:17")
		"javafx.swing", // implementation("org.openjfx:javafx-swing:17")
	)
}

java {
	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<JavaCompile>().configureEach {
	options.release = 17
	options.compilerArgs = options.compilerArgs + listOf(
		"-Xlint:unchecked",
		"-Xlint:deprecation",
	)
}

kotlin {
	compilerOptions {
		jvmTarget = JvmTarget.fromTarget("17")
		allWarningsAsErrors = true
		extraWarnings = true
		freeCompilerArgs.addAll(
			// Opt in to https://youtrack.jetbrains.com/issue/KT-59109 for now to see how to suppress warnings/errors.
			"-Xrender-internal-diagnostic-names",
		)
	}
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

kover {
	reports {
		total {
			verify {
				onCheck = false
			}
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
