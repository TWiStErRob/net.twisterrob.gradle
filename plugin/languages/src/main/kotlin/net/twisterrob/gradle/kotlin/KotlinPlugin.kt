package net.twisterrob.gradle.kotlin

import com.android.build.api.dsl.CommonExtension
import net.twisterrob.gradle.android.hasAndroid
import net.twisterrob.gradle.android.hasAndroidTest
import net.twisterrob.gradle.android.srcDirCompat
import net.twisterrob.gradle.base.shouldAddAutoRepositoriesTo
import net.twisterrob.gradle.common.AGPVersions
import net.twisterrob.gradle.common.BasePlugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByName
import java.util.Locale
import kotlin.reflect.KCallable

private typealias DependencyAdder = DependencyHandler.(Any) -> Dependency?

const val VERSION_KOTLIN: String = "1.4.32"

@Suppress("detekt.UnnecessaryAbstractClass") // Gradle convention.
abstract class KotlinPlugin : BasePlugin() {

	override fun apply(target: Project) {
		super.apply(target)

		if (project.plugins.hasAndroid()) {
			if (AGPVersions.CLASSPATH < AGPVersions.v9xx) {
				// CONSIDER https://github.com/griffio/dagger2-kotlin/blob/b62c06aa8ebdc78f2d3ba95ca29fd79bff77b848/README.md
				project.plugins.apply("org.jetbrains.kotlin.android")
				project.plugins.apply("org.jetbrains.kotlin.kapt")
			}
			if (shouldAddAutoRepositoriesTo(project)) {
				project.repositories.mavenCentral()
			}
			project.dependencies.implementation(kotlin("stdlib-jdk7"))
			if (project.plugins.hasAndroidTest()) {
				project.addTestDependencies(DependencyHandler::implementation)
			} else {
				project.addTestDependencies(DependencyHandler::testImplementation)
			}
			val android = project.extensions.getByName<CommonExtension>("android")
			// TODEL https://youtrack.jetbrains.com/issue/KT-80985
			@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
			android.sourceSets.configureEach {
				it.java.srcDirCompat("src/${it.name}/kotlin")
			}
		} else {
			project.plugins.apply("org.jetbrains.kotlin.jvm")
			if (shouldAddAutoRepositoriesTo(project)) {
				project.repositories.mavenCentral()
			}
			project.dependencies.implementation(kotlin("stdlib"))
			project.addTestDependencies(DependencyHandler::testImplementation)
		}
	}

	companion object {

		private fun kotlin(module: String): String =
			"org.jetbrains.kotlin:kotlin-$module:$VERSION_KOTLIN"

		private fun Project.addTestDependencies(configuration: DependencyAdder) {
			dependencies.configuration(kotlin("test"))
			addKotlinJUnitIfNeeded(configuration)
			addKotlinTestNGIfNeeded(configuration)
		}

		private fun Project.addKotlinJUnitIfNeeded(configuration: DependencyAdder) {
			addIfNeeded(configuration, Dependency::isJUnit4, kotlin("test-junit"))
		}

		private fun Project.addKotlinTestNGIfNeeded(configuration: DependencyAdder) {
			addIfNeeded(configuration, Dependency::isTestNG, kotlin("test-testng"))
		}

		private fun Project.addIfNeeded(
			configuration: DependencyAdder, existingDependency: Dependency.() -> Boolean, newDependency: String
		) {
			val name = (configuration as KCallable<*>).name
			val realConfiguration = configurations[name]
			realConfiguration.allDependencies.configureEach { dep ->
				if (dep.existingDependency()) {
					val extraName = "twisterrobKotlin${name.replaceFirstChar { it.uppercase(Locale.ROOT) }}"
					val extraConfiguration = configurations.create(extraName) { temp ->
						temp.isCanBeConsumed = false
						temp.isCanBeResolved = false
						temp.defaultDependencies { dependencies ->
							dependencies.add(project.dependencies.create(newDependency))
						}
					}
					realConfiguration.extendsFrom(extraConfiguration)
				}

			}
		}
	}
}

private fun DependencyHandler.implementation(dependencyNotation: Any): Dependency? =
	add("implementation", dependencyNotation)

private fun DependencyHandler.testImplementation(dependencyNotation: Any): Dependency? =
	add("testImplementation", dependencyNotation)

private fun Dependency.isJUnit4(): Boolean =
	this.group == "junit" && this.name == "junit"
			&& this.version.orEmpty().matches("""4\.\d+(\.\d+)?(-SNAPSHOT|-\d{8}\.\d{6}-\d+)?""".toRegex())

private fun Dependency.isTestNG(): Boolean =
	this.group == "org.testng" && this.name == "testng"
