import org.gradle.api.JavaVersion
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.DependencyResolveDetails

object Libs {

	object Kotlin {
		fun Configuration.replaceKotlinJre7WithJdk7() {
			resolutionStrategy.eachDependency { replaceJreWithJdk(7) }
		}

		fun Configuration.replaceKotlinJre8WithJdk8() {
			resolutionStrategy.eachDependency { replaceJreWithJdk(8) }
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
					"kotlin-stdlib-jre$version" -> useTarget("${target.group}:kotlin-stdlib-jdk$version:${target.version}")
				}
			}
		}
	}

	object Hamcrest {
		fun Configuration.replaceHamcrestDependencies() {
			resolutionStrategy.eachDependency { replaceHamcrestDependencies() }
		}

		/**
		 * https://github.com/junit-team/junit4/pull/1608#issuecomment-496238766
		 */
		private fun DependencyResolveDetails.replaceHamcrestDependencies() {
			if (requested.group == "org.hamcrest") {
				when (requested.name) {
					"java-hamcrest" -> {
						useTarget("org.hamcrest:hamcrest:2.2")
						because("2.0.0.0 shouldn't have been published")
					}
					"hamcrest-core" -> { // Could be 1.3 (JUnit 4) or 2.x too.
						useTarget("org.hamcrest:hamcrest:2.2")
						because("hamcrest-core doesn't contain anything")
					}
					"hamcrest-library" -> {
						useTarget("org.hamcrest:hamcrest:2.2")
						because("hamcrest-library doesn't contain anything")
					}
				}
			}
		}
	}

	val javaVersion = JavaVersion.VERSION_1_8
}
