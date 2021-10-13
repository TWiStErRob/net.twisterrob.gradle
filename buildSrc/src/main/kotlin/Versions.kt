import Libs.JUnit5.api
import Libs.JUnit5.engine
import Libs.JUnit5.vintage
import org.gradle.api.JavaVersion
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.DependencyResolveDetails

object Libs {

	object Annotations {

		/**
		 * @see <a href="https://repo1.maven.org/maven2/com/google/code/findbugs/jsr305/">Artifacts</a>
		 */
		private const val versionJsr305 = "3.0.2"

		/**
		 * @see <a href="https://github.com/JetBrains/java-annotations">Repository</a>
		 * @see <a href="https://repo1.maven.org/maven2/org/jetbrains/annotations/">Artifacts</a>
		 */
		private const val versionJetbrains = "21.0.0"

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
		/**
		 * @see versionLint which is affected by this
		 */
		private const val versionAndroidGradlePlugin = "4.2.2"

		/**
		 * = 23.0.0 + [versionAndroidGradlePlugin].
		 *
		 * @see com.android.build.gradle.internal.plugins.BasePlugin.createLintClasspathConfiguration
		 * @see `builder-model//version.properties`
		 */
		@Suppress("KDocUnresolvedReference")
		private const val versionLint = "27.2.2"

		/**
		 * @see <a href="https://developer.android.com/studio/releases/gradle-plugin#updating-gradle">Compatibility</a>
		 */
		const val plugin = "com.android.tools.build:gradle:${versionAndroidGradlePlugin}"

		const val lint = "com.android.tools.lint:lint:${versionLint}"
		const val lintApi = "com.android.tools.lint:lint-api:${versionLint}"
		const val lintGradle = "com.android.tools.lint:lint-gradle:${versionLint}"
		const val lintGradleApi = "com.android.tools.lint:lint-gradle-api:${versionLint}"
		const val lintChecks = "com.android.tools.lint:lint-checks:${versionLint}"
		const val toolsDddmlib = "com.android.tools.ddms:ddmlib:${versionLint}"
		const val toolsCommon = "com.android.tools:common:${versionLint}"
	}

	object Kotlin {
		/**
		 * @see <a href="https://github.com/JetBrains/kotlin/blob/master/ChangeLog.md">Changelog</a>
		 * @see kotlin_version in buildSrc/gradle.properties
		 */
		@Suppress("KDocUnresolvedReference")
		private const val version = "1.4.32"

		/**
		 * @see <a href="https://github.com/gradle/kotlin-dsl/releases">GitHub Releases</a>
		 * @see <a href="https://repo.gradle.org/gradle/libs-releases-local/org/gradle/gradle-kotlin-dsl/">Artifacts</a>
		 * TODO there's no later version that 6.1.1, even though Gradle is 6.9 / 7.x already.
		 */
		private const val versionDSL = "6.1.1"

		const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib:${version}"
		const val reflect = "org.jetbrains.kotlin:kotlin-reflect:${version}"
		const val test = "org.jetbrains.kotlin:kotlin-test:${version}"
		const val stdlibJdk7 = "org.jetbrains.kotlin:kotlin-stdlib-jre8:${version}"
		const val stdlibJdk8 = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${version}"
		const val gradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${version}"

		const val dsl = "org.gradle:gradle-kotlin-dsl:${versionDSL}"

		@Deprecated("Don't use directly", replaceWith = ReplaceWith("stdlibJdk7"))
		const val stdlibJre7 = "org.jetbrains.kotlin:kotlin-stdlib-jre7:${version}"
		@Deprecated("Don't use directly", replaceWith = ReplaceWith("stdlibJdk8"))
		const val stdlibJre8 = "org.jetbrains.kotlin:kotlin-stdlib-jre8:${version}"

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

		const val library = "junit:junit:${version}"
	}

	/**
	 * JUnit 5 = JUnit Platform ([api]) + JUnit Jupiter ([engine]) + JUnit Vintage ([vintage])
	 */
	object JUnit5 {

		/**
		 * @see <a href="https://junit.org/junit5/docs/current/release-notes/index.html">Changelog</a>
		 */
		private const val version = "5.8.1"

		/**
		 * @see <a href="https://github.com/junit-pioneer/junit-pioneer/releases">GitHub Releases</a>
		 */
		private const val pioneerVersion = "1.4.2"

		const val api = "org.junit.jupiter:junit-jupiter-api:${version}"
		const val params = "org.junit.jupiter:junit-jupiter-params:${version}"
		/**
		 * `runtimeOnly` dependency, because it implements some interfaces from [api], but doesn't need to be visible to user.
		 * @see <a href="https://junit.org/junit5/docs/current/user-guide/index.html#running-tests-build-gradle-engines-configure">Engines</a>
		 */
		const val engine = "org.junit.jupiter:junit-jupiter-engine:${version}"
		/**
		 * `runtimeOnly` dependency, because it implements some interfaces from [api], but doesn't need to be visible to user.
		 */
		const val vintage = "org.junit.vintage:junit-vintage-engine:${version}"

		const val pioneer = "org.junit-pioneer:junit-pioneer:${pioneerVersion}"
	}

	object Mockito {
		/**
		 * @see <a href="https://mvnrepository.com/artifact/org.mockito/mockito-core">Artifacts</a>
		 * @see <a href="https://github.com/mockito/mockito/blob/release/2.x/doc/release-notes/official.md">Changelog</a>
		 */
		private const val version = "3.10.0"

		/**
		 * @see <a href="https://github.com/nhaarman/mockito-kotlin/releases">GitHub releases</a>
		 */
		private const val versionMockitoKotlin = "3.2.0"

		const val core = "org.mockito:mockito-core:${version}"
		const val junit5 = "org.mockito:mockito-junit-jupiter:${version}"
		const val kotlin = "org.mockito.kotlin:mockito-kotlin:${versionMockitoKotlin}"
	}

	object Hamcrest {
		private const val version = "2.2"

		const val best = "org.hamcrest:hamcrest:${version}"

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
						useTarget("org.hamcrest:hamcrest:${version}")
						because("2.0.0.0 shouldn't have been published")
					}
					"hamcrest-core" -> { // Could be 1.3 (JUnit 4) or 2.x too.
						useTarget("org.hamcrest:hamcrest:${version}")
						because("hamcrest-core doesn't contain anything")
					}
					"hamcrest-library" -> {
						useTarget("org.hamcrest:hamcrest:${version}")
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
		private const val version = "2.7.2"

		const val java = "com.flextrade.jfixture:jfixture:${version}"
		const val kotlin = "com.flextrade.jfixture:kfixture:1.0.0"
	}

	object SVNKit {

		/**
		 * @see <a href="https://mvnrepository.com/artifact/org.tmatesoft.svnkit/svnkit">Versions</a>
		 */
		private const val version = "1.10.3"
		const val core = "org.tmatesoft.svnkit:svnkit:${version}"
		const val cli = "org.tmatesoft.svnkit:svnkit-cli:${version}"
	}

	val javaVersion = JavaVersion.VERSION_1_8

	/**
	 * @see <a href="https://github.com/mockk/mockk/releases">GitHub releases</a>
	 */
	private const val mockkVersion = "1.11.0"
	const val mockk = "io.mockk:mockk:${mockkVersion}"

	/**
	 * @see <a href="https://github.com/tomasbjerre/violations-lib/blob/master/CHANGELOG.md">Changelog</a>
	 * @see <a href="https://github.com/tomasbjerre/violations-lib/releases">GitHub Releases</a>
	 */
	private const val violationsVersion = "1.81"
	const val violations = "se.bjurr.violations:violations-lib:${violationsVersion}"

	private const val guavaVersion = "22.0"
	const val guava = "com.google.guava:guava:${guavaVersion}"

	/**
	 * @see <a href="https://mvnrepository.com/artifact/org.eclipse.jgit/org.eclipse.jgit">Version history</a>
	 * @see <a href="https://projects.eclipse.org/projects/technology.jgit">Changelog (Full)</a>
	 * @see <a href="https://wiki.eclipse.org/JGit/New_and_Noteworthy">Changelog (Summary)</a>
	 */
	private const val jgitVersion = "5.13.0.202109080827-r"
	const val jgit = "org.eclipse.jgit:org.eclipse.jgit:${jgitVersion}"

	private const val dexMemberListVersion = "4.1.1"
	const val dexMemberList = "com.jakewharton.dex:dex-member-list:${dexMemberListVersion}"
}
