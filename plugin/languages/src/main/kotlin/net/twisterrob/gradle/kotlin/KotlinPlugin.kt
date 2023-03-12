package net.twisterrob.gradle.kotlin

import com.android.build.gradle.BaseExtension
import net.twisterrob.gradle.android.hasAndroid
import net.twisterrob.gradle.android.hasAndroidTest
import net.twisterrob.gradle.base.shouldAddAutoRepositoriesTo
import net.twisterrob.gradle.common.BasePlugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.get
import kotlin.reflect.KCallable

private typealias DependencyAdder = DependencyHandler.(Any) -> Dependency?

const val VERSION_KOTLIN: String = "1.4.32"

class KotlinPlugin : BasePlugin() {

	override fun apply(target: Project) {
		super.apply(target)

		if (project.plugins.hasAndroid()) {
			// CONSIDER https://github.com/griffio/dagger2-kotlin/blob/master/README.md
			project.plugins.apply("org.jetbrains.kotlin.android")
			project.plugins.apply("org.jetbrains.kotlin.kapt")
			if (shouldAddAutoRepositoriesTo(project)) {
				project.repositories.mavenCentral()
			}
			project.dependencies.implementation(kotlin("stdlib-jdk7"))
			if (project.plugins.hasAndroidTest()) {
				project.addTestDependencies(DependencyHandler::implementation)
			} else {
				project.addTestDependencies(DependencyHandler::testImplementation)
			}
			val android: BaseExtension = project.extensions["android"] as BaseExtension
			android.sourceSets.all {
				it.java.srcDir("src/${it.name}/kotlin")
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
			configurations[(configuration as KCallable<*>).name].dependencies.all { dep ->
				if (dep.group == "junit" && dep.name == "junit"
					&& dep.version.orEmpty().matches("""4\.\d+(\.\d+)?(-SNAPSHOT|-\d{8}\.\d{6}-\d+)?""".toRegex())
				) {
					dependencies.configuration(kotlin("test-junit"))
				}
			}
		}

		private fun Project.addKotlinTestNGIfNeeded(configuration: DependencyAdder) {
			configurations[(configuration as KCallable<*>).name].dependencies.all { dep ->
				if (dep.group == "org.testng" && dep.name == "testng") {
					dependencies.configuration(kotlin("test-testng"))
				}
			}
		}
	}
}

private fun DependencyHandler.implementation(dependencyNotation: Any): Dependency? =
	add("implementation", dependencyNotation)

private fun DependencyHandler.testImplementation(dependencyNotation: Any): Dependency? =
	add("testImplementation", dependencyNotation)
