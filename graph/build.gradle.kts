import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.text.SimpleDateFormat
import java.util.Date

plugins {
	id("java-gradle-plugin")
	id("org.jetbrains.kotlin.jvm") version "1.8.22"
	id("org.openjfx.javafxplugin") version "0.0.14"
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

dependencies {
	implementation(gradleApi())
	implementation("org.graphstream:gs-core:1.3")
	implementation("com.google.code.gson:gson:2.10.1")
	testImplementation("junit:junit:4.13.2")
}

javafx {
	modules = listOf(
		"javafx.controls",
		"javafx.web",
		"javafx.swing",
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
