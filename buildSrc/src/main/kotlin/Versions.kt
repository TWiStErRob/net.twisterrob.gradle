import org.gradle.api.JavaVersion
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.DependencyResolveDetails

object Libs {

	object Annotations {

		/**
		 * @see <a href="https://github.com/JetBrains/java-annotations">Repository</a>
		 * @see <a href="https://repo1.maven.org/maven2/org/jetbrains/annotations/">Artifacts</a>
		 */
		private const val jetbrains = "21.0.0"
	}

	object Android {
		/**
		 * @see lint which is affected by this
		 */
		private const val agp = "4.2.2"

		/**
		 * = 23.0.0 + [agp].
		 *
		 * @see com.android.build.gradle.internal.plugins.BasePlugin.createLintClasspathConfiguration
		 * @see `builder-model//version.properties`
		 */
		@Suppress("KDocUnresolvedReference")
		private const val lint = "27.2.2"
	}

	object Kotlin {
		/**
		 * @see <a href="https://github.com/JetBrains/kotlin/blob/master/ChangeLog.md">Changelog</a>
		 * @see kotlin_version in buildSrc/gradle.properties
		 */
		@Suppress("KDocUnresolvedReference")
		private const val kotlin = "1.4.32"

		/**
		 * @see <a href="https://github.com/gradle/kotlin-dsl/releases">GitHub Releases</a>
		 * @see <a href="https://repo.gradle.org/gradle/libs-releases-local/org/gradle/gradle-kotlin-dsl/">Artifacts</a>
		 * TODO there's no later version that 6.1.1, even though Gradle is 6.9 / 7.x already.
		 */
		private const val `kotlin-dsl` = "6.1.1"

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

	object JUnit4 {

		/**
		 * @see <a href="https://github.com/junit-team/junit4/tree/main/doc">Release notes</a>
		 * @see <a href="https://github.com/junit-team/junit4/wiki">Major releases</a>
		 */
		private const val version = "4.13.2"
	}

	/**
	 * JUnit 5 = JUnit Platform (api) + JUnit Jupiter (engine) + JUnit Vintage (vintage)
	 */
	object JUnit5 {

		/**
		 * @see <a href="https://junit.org/junit5/docs/current/release-notes/index.html">Changelog</a>
		 */
		private const val junit5 = "5.8.1"

		/**
		 * @see <a href="https://github.com/junit-pioneer/junit-pioneer/releases">GitHub Releases</a>
		 */
		private const val `junit5.pioneer` = "1.4.2"
	}

	object Mockito {
		/**
		 * @see <a href="https://mvnrepository.com/artifact/org.mockito/mockito-core">Artifacts</a>
		 * @see <a href="https://github.com/mockito/mockito/blob/release/2.x/doc/release-notes/official.md">Changelog</a>
		 */
		private const val mockito = "3.10.0"

		/**
		 * @see <a href="https://github.com/nhaarman/mockito-kotlin/releases">GitHub releases</a>
		 */
		private const val `mockito.kotlin` = "3.2.0"
	}

	object Hamcrest {
		private const val hamcrest = "2.2"

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
						useTarget("org.hamcrest:hamcrest:${this@Hamcrest.hamcrest}")
						because("2.0.0.0 shouldn't have been published")
					}
					"hamcrest-core" -> { // Could be 1.3 (JUnit 4) or 2.x too.
						useTarget("org.hamcrest:hamcrest:${this@Hamcrest.hamcrest}")
						because("hamcrest-core doesn't contain anything")
					}
					"hamcrest-library" -> {
						useTarget("org.hamcrest:hamcrest:${this@Hamcrest.hamcrest}")
						because("hamcrest-library doesn't contain anything")
					}
				}
			}
		}
	}

	object JFixture {
		/**
		 * @see <a href="https://github.com/FlexTradeUKLtd/jfixture/releases">GitHub releases</a>
		 */
		private const val jfixture = "2.7.2"
	}

	object SVNKit {

		/**
		 * @see <a href="https://mvnrepository.com/artifact/org.tmatesoft.svnkit/svnkit">Versions</a>
		 */
		private const val svnkit = "1.10.3"
	}

	val javaVersion = JavaVersion.VERSION_1_8

	/**
	 * @see <a href="https://github.com/mockk/mockk/releases">GitHub releases</a>
	 */
	private const val mockk = "1.11.0"

	/**
	 * @see <a href="https://github.com/tomasbjerre/violations-lib/blob/master/CHANGELOG.md">Changelog</a>
	 * @see <a href="https://github.com/tomasbjerre/violations-lib/releases">GitHub Releases</a>
	 */
	private const val violations = "1.81"

	private const val guava = "22.0"

	/**
	 * @see <a href="https://mvnrepository.com/artifact/org.eclipse.jgit/org.eclipse.jgit">Version history</a>
	 * @see <a href="https://projects.eclipse.org/projects/technology.jgit">Changelog (Full)</a>
	 * @see <a href="https://wiki.eclipse.org/JGit/New_and_Noteworthy">Changelog (Summary)</a>
	 */
	private const val jgit = "5.13.0.202109080827-r"

	private const val dexMemberList = "4.1.1"
}
