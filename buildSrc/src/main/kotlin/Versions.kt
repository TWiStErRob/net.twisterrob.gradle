import Libs.JUnit5.api
import Libs.JUnit5.engine
import Libs.JUnit5.vintage
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

		const val JUnit4 = "4.13"

		/**
		 * @see <a href="https://junit.org/junit5/docs/current/release-notes/index.html">Changelog</a>
		 */
		const val JUnit5 = "5.4.0"
	}

	object Annotations {

		private const val versionJsr305 = "3.0.2"

		/**
		 * @see <a href="https://github.com/JetBrains/java-annotations">Repository</a>
		 * @see <a href="http://repo1.maven.org/maven2/org/jetbrains/annotations/">Artifacts</a>
		 */
		private const val versionJetbrains = "17.0.0"

		/**
		 * <a href="https://jcp.org/en/jsr/detail?id=305">JSR 305: Annotations for Software Defect Detection</a>
		 */
		const val jsr305 = "com.google.code.findbugs:jsr305:${versionJsr305}"

		/**
		 * @see <a href="https://www.jetbrains.com/help/idea/annotating-source-code.html">Documentation</a>
		 */
		const val jetbrains = "org.jetbrains:annotations:${versionJetbrains}"
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

	object JUnit4 {
		const val library = "junit:junit:${Versions.JUnit4}"
	}

	/**
	 * JUnit 5 = JUnit Platform ([api]) + JUnit Jupiter ([engine]) + JUnit Vintage ([vintage])
	 */
	object JUnit5 {

		const val api = "org.junit.jupiter:junit-jupiter-api:${Versions.JUnit5}"
		const val params = "org.junit.jupiter:junit-jupiter-params:${Versions.JUnit5}"
		/**
		 * `runtimeOnly` dependency, because it implements some interfaces from [api], but doesn't need to be visible to user.
		 * @see <a href="https://junit.org/junit5/docs/current/user-guide/index.html#running-tests-build-gradle-engines-configure">Engines</a>
		 */
		const val engine = "org.junit.jupiter:junit-jupiter-engine:${Versions.JUnit5}"
		/**
		 * `runtimeOnly` dependency, because it implements some interfaces from [api], but doesn't need to be visible to user.
		 */
		const val vintage = "org.junit.vintage:junit-vintage-engine:${Versions.JUnit5}"
	}

	object Mockito {
		/**
		 * @see <a href="https://bintray.com/mockito/maven">Artifacts</a>
		 * @see <a href="https://github.com/mockito/mockito/blob/release/2.x/doc/release-notes/official.md">Changelog</a>
		 */
		private const val version = "2.24.4"

		/**
		 * @see <a href="https://github.com/nhaarman/mockito-kotlin/releases">GitHub releases</a>
		 */
		private const val versionMockitoKotlin = "2.1.0"

		const val core = "org.mockito:mockito-core:${version}"
		const val junit5 = "org.mockito:mockito-junit-jupiter:${version}"
		const val kotlin = "com.nhaarman.mockitokotlin2:mockito-kotlin:${versionMockitoKotlin}"
	}

	object Hamcrest {
		private const val versionNew = "2.0.0.0"

		const val new = "org.hamcrest:java-hamcrest:${versionNew}"
	}
}
