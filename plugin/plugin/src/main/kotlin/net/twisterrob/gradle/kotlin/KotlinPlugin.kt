package net.twisterrob.gradle.kotlin

import com.android.build.gradle.BaseExtension
import net.twisterrob.gradle.android.hasAndroid
import net.twisterrob.gradle.base.BasePlugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.get

const val VERSION_KOTLIN = "1.2.71"

class KotlinPlugin : BasePlugin() {

	override fun apply(target: Project) {
		super.apply(target)

		if (project.plugins.hasAndroid()) {
			// CONSIDER https://github.com/griffio/dagger2-kotlin/blob/master/README.md
			project.plugins.apply("kotlin-android")
			project.plugins.apply("kotlin-kapt")
			project.repositories.jcenter()
			project.dependencies.add("implementation", "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$VERSION_KOTLIN")
			project.dependencies.add("testImplementation", "org.jetbrains.kotlin:kotlin-test:$VERSION_KOTLIN")
			val android: BaseExtension = project.extensions["android"] as BaseExtension
			android.sourceSets.all {
				it.java.srcDir("src/${it.name}/kotlin")
			}
		} else {
			project.plugins.apply("kotlin")
			project.repositories.jcenter()
			project.dependencies.add("implementation", "org.jetbrains.kotlin:kotlin-stdlib:$VERSION_KOTLIN")
			project.configurations["testImplementation"].dependencies.all {
				if (it.group == "junit" && it.name == "junit"
					&& (it.version ?: "").matches("""4.\d+(-SNAPSHOT|-\d{8}\.\d{6}-\d+)?""".toRegex())
				) {
					project.dependencies.add(
						"testImplementation",
						"org.jetbrains.kotlin:kotlin-test-junit:$VERSION_KOTLIN"
					)
				}
			}
			project.dependencies.add("testImplementation", "org.jetbrains.kotlin:kotlin-test:$VERSION_KOTLIN")
		}
	}
}
