import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.DependencyResolveDetails

object Libs {
	private object Versions {
		/**
		 * @see <a href="https://github.com/JetBrains/kotlin/blob/master/ChangeLog.md">Changelog</a>
		 */
		const val Kotlin = "1.3.50"

		/**
		 * @see <a href="https://github.com/gradle/kotlin-dsl/releases">GitHub Releases</a>
		 * @see <a href="https://repo.gradle.org/gradle/libs-releases-local/org/gradle/gradle-kotlin-dsl/">Artifacts</a>
		 */
		const val KotlinDSL = "5.6.4"

		/**
		 * @see AndroidLint which is affected by this
		 */
		const val AndroidGradlePlugin = "3.5.3"

		/**
		 * @see com.android.build.gradle.BasePlugin.createLintClasspathConfiguration
		 * @see `builder-model//version.properties`
		 * @see VERSION_LINT in `gradle.properties`
		 */
		@Suppress("KDocUnresolvedReference")
		const val AndroidLint = "26.5.3"
	}

	object Android {
		const val plugin = "com.android.tools.build:gradle:${Versions.AndroidGradlePlugin}"
	}

	object Kotlin {
		const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.Kotlin}"
		const val reflect = "org.jetbrains.kotlin:kotlin-reflect:${Versions.Kotlin}"
		const val test = "org.jetbrains.kotlin:kotlin-test:${Versions.Kotlin}"
		const val stdlibJdk7 = "org.jetbrains.kotlin:kotlin-stdlib-jre8:${Versions.Kotlin}"
		const val stdlibJdk8 = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.Kotlin}"

		const val dsl = "org.gradle:gradle-kotlin-dsl:${Versions.KotlinDSL}"

		@Deprecated("Don't use directly", replaceWith = ReplaceWith("stdlibJdk7"))
		const val stdlibJre7 = "org.jetbrains.kotlin:kotlin-stdlib-jre7:${Versions.Kotlin}"
		@Deprecated("Don't use directly", replaceWith = ReplaceWith("stdlibJdk8"))
		const val stdlibJre8 = "org.jetbrains.kotlin:kotlin-stdlib-jre8:${Versions.Kotlin}"

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
				because("http://kotlinlang.org/docs/reference/whatsnew12.html#kotlin-standard-library-artifacts-and-split-packages")
				when (requested.name) {
					"kotlin-stdlib-jre$version" -> useTarget("${target.group}:kotlin-stdlib-jdk$version:${target.version}")
				}
			}
		}
	}
}
