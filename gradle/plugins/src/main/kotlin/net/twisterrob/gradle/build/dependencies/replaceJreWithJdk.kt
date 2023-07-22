package net.twisterrob.gradle.build.dependencies

import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.DependencyResolveDetails

fun Configuration.replaceKotlinJre7WithJdk7() {
	resolutionStrategy.eachDependency { replaceJreWithJdk(@Suppress("MagicNumber") 7) }
}

fun Configuration.replaceKotlinJre8WithJdk8() {
	resolutionStrategy.eachDependency { replaceJreWithJdk(@Suppress("MagicNumber") 8) }
}

/**
 * @receiver `project.configurations.all { resolutionStrategy.eachDependency { ... } }`
 * @see <a href="http://kotlinlang.org/docs/reference/whatsnew12.html#kotlin-standard-library-artifacts-and-split-packages">Kotlin 1.2 change</a>
 * @see <a href="https://issuetracker.google.com/issues/72274424">Android Gradle Plugin issue</a>
 */
private fun DependencyResolveDetails.replaceJreWithJdk(version: Int) {
	if (requested.group == "org.jetbrains.kotlin") {
		because("https://kotlinlang.org/docs/reference/whatsnew12.html#kotlin-standard-library-artifacts-and-split-packages")
		when (requested.name) {
			"kotlin-stdlib-jre${version}" -> {
				val targetVersion = target.version ?: error("No version in ${this} / ${this.target}")
				useTarget("${target.group}:kotlin-stdlib-jdk${version}:${targetVersion}")
			}
		}
	}
}
