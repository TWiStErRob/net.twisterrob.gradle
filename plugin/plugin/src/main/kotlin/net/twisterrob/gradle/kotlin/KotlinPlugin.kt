package net.twisterrob.gradle.kotlin

import com.android.build.gradle.BaseExtension
import net.twisterrob.gradle.android.hasAndroid
import net.twisterrob.gradle.android.hasAndroidTest
import net.twisterrob.gradle.base.BasePlugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.get

const val VERSION_KOTLIN = "1.3.72"

class KotlinPlugin : BasePlugin() {

	override fun apply(target: Project) {
		super.apply(target)

		if (project.plugins.hasAndroid()) {
			// CONSIDER https://github.com/griffio/dagger2-kotlin/blob/master/README.md
			project.plugins.apply("kotlin-android")
			project.plugins.apply("kotlin-kapt")
			project.repositories.jcenter()
			project.dependencies.add("implementation", kotlin("stdlib-jdk7"))
			if (project.plugins.hasAndroidTest()) {
				project.addTestDependencies("implementation")
			} else {
				project.addTestDependencies("testImplementation")
			}
			val android: BaseExtension = project.extensions["android"] as BaseExtension
			android.sourceSets.all {
				it.java.srcDir("src/${it.name}/kotlin")
			}
		} else {
			project.plugins.apply("kotlin")
			project.repositories.jcenter()
			project.dependencies.add("implementation", kotlin("stdlib"))
			project.addTestDependencies("testImplementation")
		}
	}

	companion object {

		private fun kotlin(module: String) = "org.jetbrains.kotlin:kotlin-$module:$VERSION_KOTLIN"

		private fun Project.addTestDependencies(configuration: String) {
			dependencies.add(configuration, kotlin("test"))
			addKotlinJUnitIfNeeded(configuration)
			addKotlinTestNGIfNeeded(configuration)
		}

		private fun Project.addKotlinJUnitIfNeeded(configuration: String) {
			configurations[configuration].dependencies.all {
				if (it.group == "junit" && it.name == "junit"
					&& (it.version ?: "").matches("""4\.\d+(\.\d+)?(-SNAPSHOT|-\d{8}\.\d{6}-\d+)?""".toRegex())
				) {
					dependencies.add(configuration, kotlin("test-junit"))
				}
			}
		}

		private fun Project.addKotlinTestNGIfNeeded(configuration: String) {
			configurations[configuration].dependencies.all {
				if (it.group == "org.testng" && it.name == "testng") {
					dependencies.add(configuration, kotlin("test-testng"))
				}
			}
		}
	}
}
