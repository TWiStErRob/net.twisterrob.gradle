package net.twisterrob.gradle.android

import junit.runner.Version
import net.twisterrob.gradle.test.GradleBuildTestResources
import net.twisterrob.gradle.test.GradleBuildTestResources.basedOn
import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.assertNoOutputLine
import net.twisterrob.gradle.test.assertSuccess
import net.twisterrob.gradle.test.fixtures.ContentMergeMode
import net.twisterrob.gradle.test.root
import org.gradle.api.artifacts.ArtifactRepositoryContainer
import org.gradle.api.internal.artifacts.dsl.DefaultRepositoryHandler
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Tests for [AndroidBuildPlugin].
 *
 *  Note: if Robolectric tests fail with
 * > java.lang.IllegalArgumentException: URI is not hierarchical
 * >     at java.base/sun.nio.fs.WindowsUriSupport.fromUri(WindowsUriSupport.java:122)
 *
 * [Delete %TEMP%\robolectric-2 folder](https://github.com/robolectric/robolectric/issues/4567#issuecomment-475740375)
 *
 * @see AndroidBuildPlugin
 * @see net.twisterrob.gradle.android.tasks.CalculateBuildTimeTask
 * @see net.twisterrob.gradle.android.tasks.CalculateVCSRevisionInfoTask
 */
@ExtendWith(GradleRunnerRuleExtension::class)
class AndroidBuildPluginIntgTest : BaseAndroidIntgTest() {

	override lateinit var gradle: GradleRunnerRule

	@Test fun `adds automatic repositories`() {
		gradle.file("", ContentMergeMode.OVERWRITE, "settings.gradle")

		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.android-app")
			}
			afterEvaluate {
				println("repoNames=" + repositories.names)
			}
		""".trimIndent()

		val result = gradle.run(script, "assembleDebug").build()

		result.assertHasOutputLine("repoNames=[${GOOGLE}, ${MAVEN_CENTRAL}]")
		result.assertSuccess(":assembleDebug")
	}

	@Test fun `does not add repositories automatically when it would fail`() {
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.android-app")
			}
			afterEvaluate {
				println("repoNames=" + repositories.names)
			}
		""".trimIndent()

		val result = gradle.run(script, "assembleDebug").build()

		result.assertHasOutputLine("repoNames=[]")
		// Build is successful without repos, because they come from settings.gradle.
		result.assertSuccess(":assembleDebug")
	}

	@Test fun `default build setup is simple and produces default output (debug)`() {
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.android-app")
			}
		""".trimIndent()

		val result = gradle.run(script, "assembleDebug").build()

		result.assertSuccess(":assembleDebug")
		assertDefaultDebugBadging(
			apk = gradle.root.apk("debug")
		)
	}

	@Test fun `default build setup is simple and produces default output (release)`() {
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.android-app")
			}
		""".trimIndent()

		val result = gradle.run(script, "assembleRelease").build()

		result.assertSuccess(":assembleRelease")
		assertDefaultReleaseBadging(
			apk = gradle.root.apk("release")
		)
	}

	@Test fun `can override minSdkVersion (debug)`() {
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.android-app")
			}
			android.defaultConfig.minSdkVersion = 10
		""".trimIndent()

		val result = gradle.run(script, "assembleDebug").build()

		result.assertSuccess(":assembleDebug")
		assertDefaultDebugBadging(
			apk = gradle.root.apk("debug"),
			minSdkVersion = 10
		)
	}

	@Test fun `can override minSdkVersion (release)`() {
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.android-app")
			}
			android.defaultConfig.minSdkVersion = 10
		""".trimIndent()

		val result = gradle.run(script, "assembleRelease").build()

		result.assertSuccess(":assembleRelease")
		assertDefaultReleaseBadging(
			apk = gradle.root.apk("release"),
			minSdkVersion = 10
		)
	}

	@Test fun `can override targetSdk (debug)`() {
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.android-app")
			}
			android.defaultConfig.targetSdk = 19
		""".trimIndent()

		val result = gradle.run(script, "assembleDebug").build()

		result.assertSuccess(":assembleDebug")
		assertDefaultDebugBadging(
			apk = gradle.root.apk("debug"),
			targetSdkVersion = 19
		)
	}

	@Test fun `can override targetSdk (release)`() {
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.android-app")
			}
			android.defaultConfig.targetSdk = 19
			android.lint.disable("ExpiredTargetSdkVersion")
		""".trimIndent()

		val result = gradle.run(script, "assembleRelease").build()

		result.assertSuccess(":assembleRelease")
		assertDefaultReleaseBadging(
			apk = gradle.root.apk("release"),
			targetSdkVersion = 19
		)
	}

	@Test fun `can override compileSdk (debug)`() {
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.android-app")
			}
			android.compileSdk = 23
		""".trimIndent()

		val result = gradle.run(script, "assembleDebug").build()

		result.assertSuccess(":assembleDebug")
		assertDefaultDebugBadging(
			apk = gradle.root.apk("debug"),
			compileSdkVersion = 23
			//compileSdkVersionName = "6.0-2704002"
		)
	}

	@Test fun `can override compileSdk (release)`() {
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.android-app")
			}
			android.compileSdk = 23
		""".trimIndent()

		val result = gradle.run(script, "assembleRelease").build()

		result.assertSuccess(":assembleRelease")
		assertDefaultReleaseBadging(
			apk = gradle.root.apk("release"),
			compileSdkVersion = 23
			//compileSdkVersionName = "6.0-2704002"
		)
	}

	@Test fun `can disable buildConfig generation (debug)`() {
		// Default build.gradle has the app plugin applied.
		gradle.buildFile.writeText(gradle.buildFile.readText().replace("id(\"com.android.application\")", ""))

		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.android-library")
			}
			android.buildFeatures.buildConfig = false
		""".trimIndent()

		val result = gradle.run(script, "assembleDebug").build()

		result.assertSuccess(":assembleDebug")
	}

	@Suppress("LongMethod") // Multiple files are listed in this one method.
	@Test fun `can disable buildConfig decoration (debug)`() {
		// Default build.gradle has the app plugin applied.
		gradle.buildFile.writeText(gradle.buildFile.readText().replace("id(\"com.android.application\")", ""))

		gradle.basedOn(GradleBuildTestResources.kotlin)

		@Language("kotlin")
		val kotlinTestClass = """
			import ${packageName}.BuildConfig
			import org.hamcrest.MatcherAssert.assertThat
			import org.junit.Test
			
			class BuildConfigTest {
				@Test fun testRevision() {
					assertThat(BuildConfig::class, hasNoConstant("REVISION"))
				}
				@Test fun testRevisionNumber() {
					assertThat(BuildConfig::class, hasNoConstant("REVISION_NUMBER"))
				}
				@Test fun testBuildTime() {
					assertThat(BuildConfig::class, hasNoConstant("BUILD_TIME"))
				}
				// not using org.hamcrest.CoreMatchers.not, because describeMismatch is not implemented.
				private fun hasNoConstant(prop: String) : org.hamcrest.Matcher<in kotlin.reflect.KClass<*>> =
					object : org.hamcrest.TypeSafeDiagnosingMatcher<kotlin.reflect.KClass<*>>() {
						override fun describeTo(description: org.hamcrest.Description) {
							description.appendText("Class has constant named ").appendValue(prop)
						}
						override fun matchesSafely(item: kotlin.reflect.KClass<*>, mismatchDescription: org.hamcrest.Description): Boolean {
							try {
								// @formatter:off
								val field = item.java.getDeclaredField(prop).apply { isAccessible = true }
								val value = try { field.get(null) } catch (ex: Exception) { ex }
								// @formatter:on
								mismatchDescription.appendValue(field).appendText(" existed with value: ").appendValue(value)
								return false
							} catch (ex: NoSuchFieldException) {
								return true
							}
						}
					}
			}
		""".trimIndent()
		gradle.file(kotlinTestClass, "src/test/kotlin/test.kt")

		@Language("properties")
		val properties = """
			android.useAndroidX=true
		""".trimIndent()
		gradle.file(properties, ContentMergeMode.APPEND, "gradle.properties")

		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.android-library")
				id("net.twisterrob.gradle.plugin.kotlin")
			}
			android.twisterrob.decorateBuildConfig = false
			dependencies {
				testImplementation("junit:junit:${Version.id()}")
				testImplementation 'org.robolectric:robolectric:4.9.2'
				// Can't use the latest version, 1.4.1-alpha01 is the last version that's compatible with Kotlin 1.4.32.
				// > e: .../transformed/core-1.5.0-api.jar!/META-INF/androidx.test.core.kotlin_module:
				// > Module was compiled with an incompatible version of Kotlin.
				// > The binary version of its metadata is 1.7.1, expected version is 1.4.2.
				testImplementation("androidx.test:core:1.4.1-alpha01")
			}
			android.testOptions.unitTests.includeAndroidResources = true
			tasks.withType(Test).configureEach {
				//noinspection UnnecessaryQualifiedReference
				testLogging.events = org.gradle.api.tasks.testing.logging.TestLogEvent.values().toList().toSet()
			}
		""".trimIndent()

		val result = gradle.run(script, "assembleDebug", "test").build()

		result.assertSuccess(":assembleDebug")
		result.assertSuccess(":testReleaseUnitTest")
		result.assertSuccess(":testDebugUnitTest")
	}

	@Suppress("LongMethod") // Multiple files are listed in this one method.
	@Test fun `adds custom resources and BuildConfig values`() {
		gradle.basedOn(GradleBuildTestResources.kotlin)

		@Language("kotlin")
		val kotlinTestClass = """
			import ${packageName}.BuildConfig
			import ${packageName}.R
			import org.junit.Test
			
			@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner::class)
			class ResourceTest {
				@Suppress("USELESS_CAST") // validate the type and nullity of values
				@Test fun test() { // using Robolectric to access resources at runtime
					val res = androidx.test.core.app.ApplicationProvider
							.getApplicationContext<android.content.Context>()
							.resources
					printProperty("in_prod", res.getBoolean(R.bool.in_prod) as Boolean)
					printProperty("in_test", res.getBoolean(R.bool.in_test) as Boolean)
					printProperty("app_package", res.getString(R.string.app_package) as String)
					printProperty("REVISION", BuildConfig.REVISION as String)
					printProperty("REVISION_NUMBER", BuildConfig.REVISION_NUMBER as Int)
					printProperty("BUILD_TIME", (BuildConfig.BUILD_TIME as java.util.Date).time)
				}
				private fun printProperty(prop: String, value: Any?) {
					println(BuildConfig.BUILD_TYPE + "." + prop + "=" + value)
				}
			}
		""".trimIndent()
		gradle.file(kotlinTestClass, "src/test/kotlin/test.kt")

		@Language("properties")
		val properties = """
			android.useAndroidX=true
		""".trimIndent()
		gradle.file(properties, ContentMergeMode.APPEND, "gradle.properties")

		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.android-app")
				id("net.twisterrob.gradle.plugin.kotlin")
			}
			dependencies {
				testImplementation("junit:junit:${Version.id()}")
				testImplementation 'org.robolectric:robolectric:4.9.2'
				// Can't use the latest version, 1.4.1-alpha01 is the last version that's compatible with Kotlin 1.4.32.
				// > e: .../transformed/core-1.5.0-api.jar!/META-INF/androidx.test.core.kotlin_module:
				// > Module was compiled with an incompatible version of Kotlin.
				// > The binary version of its metadata is 1.7.1, expected version is 1.4.2.
				testImplementation("androidx.test:core:1.4.1-alpha01")
			}
			android.testOptions.unitTests.includeAndroidResources = true
			tasks.withType(Test).configureEach {
				//noinspection UnnecessaryQualifiedReference
				testLogging.events = org.gradle.api.tasks.testing.logging.TestLogEvent.values().toList().toSet()
			}
		""".trimIndent()

		val result = gradle.run(script, "test").build()

		val today = LocalDate.now().atStartOfDay()
		val todayMillis = today.toEpochSecond(ZoneOffset.systemDefault().rules.getOffset(today)) * 1000
		result.assertSuccess(":testReleaseUnitTest")
		result.assertHasOutputLine("    release.app_package=${packageName}")
		result.assertHasOutputLine("    release.in_prod=true")
		result.assertHasOutputLine("    release.in_test=false")
		result.assertHasOutputLine("    release.REVISION=no VCS")
		result.assertHasOutputLine("    release.REVISION_NUMBER=0")
		result.assertHasOutputLine("    release.BUILD_TIME=${todayMillis}")
		result.assertSuccess(":testDebugUnitTest")
		result.assertHasOutputLine("    debug.app_package=${packageName}.debug")
		result.assertHasOutputLine("    debug.in_prod=false")
		result.assertHasOutputLine("    debug.in_test=true")
		result.assertHasOutputLine("    debug.REVISION=no VCS")
		result.assertHasOutputLine("    debug.REVISION_NUMBER=0")
		result.assertHasOutputLine("    debug.BUILD_TIME=${todayMillis}")
	}

	@Test fun `can customize build time`() {
		gradle.basedOn(GradleBuildTestResources.kotlin)

		@Language("kotlin")
		val kotlinTestClass = """
			import ${packageName}.BuildConfig
			import ${packageName}.R
			import org.junit.Test
			
			@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner::class)
			class ResourceTest {
				@Suppress("USELESS_CAST") // validate the type and nullity of values
				@Test fun test() { // using Robolectric to access resources at runtime
					printProperty("BUILD_TIME", (BuildConfig.BUILD_TIME as java.util.Date).time)
				}
				private fun printProperty(prop: String, value: Any?) {
					println(BuildConfig.BUILD_TYPE + "." + prop + "=" + value)
				}
			}
		""".trimIndent()
		gradle.file(kotlinTestClass, "src/test/kotlin/test.kt")

		@Language("properties")
		val properties = """
			android.useAndroidX=true
		""".trimIndent()
		gradle.file(properties, ContentMergeMode.APPEND, "gradle.properties")

		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.android-app")
				id("net.twisterrob.gradle.plugin.kotlin")
			}
			dependencies {
				testImplementation("junit:junit:${Version.id()}")
				testImplementation 'org.robolectric:robolectric:4.9.2'
			}
			android.testOptions.unitTests.includeAndroidResources = true
			tasks.withType(Test).configureEach {
				//noinspection UnnecessaryQualifiedReference
				testLogging.events = org.gradle.api.tasks.testing.logging.TestLogEvent.values().toList().toSet()
			}
			tasks.named("calculateBuildConfigBuildTime").configure { buildTime.set(1234567890L) }
		""".trimIndent()

		val result = gradle.run(script, "testReleaseUnitTest").build()

		result.assertSuccess(":testReleaseUnitTest")
		result.assertHasOutputLine("    release.BUILD_TIME=1234567890")
	}

	/**
	 * @see AndroidBuildPlugin.fixVariantTaskGroups
	 */
	@Test fun `metadata of compilation tasks is present`() {
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.android-app")
			}
		""".trimIndent()

		val result = gradle.run(script, "tasks").build()

		result.assertHasOutputLine("""^compileDebugSources - (.+)""".toRegex())
		result.assertHasOutputLine("""^compileReleaseSources - (.+)""".toRegex())
		result.assertHasOutputLine("""^compileDebugJavaWithJavac - (.+)""".toRegex())
		result.assertHasOutputLine("""^compileReleaseJavaWithJavac - (.+)""".toRegex())
		result.assertHasOutputLine("""^compileDebugUnitTestSources - (.+)""".toRegex())
		result.assertHasOutputLine("""^compileReleaseUnitTestSources - (.+)""".toRegex())
		result.assertHasOutputLine("""^compileDebugUnitTestJavaWithJavac - (.+)""".toRegex())
		result.assertHasOutputLine("""^compileReleaseUnitTestJavaWithJavac - (.+)""".toRegex())
		result.assertHasOutputLine("""^compileDebugAndroidTestSources - (.+)""".toRegex())
		result.assertNoOutputLine("""^compileReleaseAndroidTestSources -(.*)""".toRegex())
		result.assertHasOutputLine("""^compileDebugAndroidTestJavaWithJavac - (.+)""".toRegex())
		result.assertNoOutputLine("""^compileReleaseAndroidTestJavaWithJavac -(.*)""".toRegex())
	}

	/**
	 * Trigger this behavior and check it doesn't happen by default.
	 * ```log
	 * Execution failed for task ':javaPreCompileDebug'.
	 * > Annotation processors must be explicitly declared now.
	 * The following dependencies on the compile classpath are found to contain annotation processor.
	 * Please add them to the annotationProcessor configuration.
	 *  - auto-service-1.0-rc6.jar (com.google.auto.service:auto-service:1.0-rc6)
	 * Alternatively, set
	 * android.defaultConfig.javaCompileOptions.annotationProcessorOptions.includeCompileClasspath = true
	 * to continue with previous behavior.
	 * Note that this option is deprecated and will be removed in the future.
	 * See https://developer.android.com/r/tools/annotation-processor-error-message.html for more details.
	 * ```
	 *
	 * Since AGP 4.0 this setting has been removed:
	 * ```log
	 * WARNING: DSL element 'annotationProcessorOptions.includeCompileClasspath' is obsolete.
	 * It will be removed in version 5.0 of the Android Gradle plugin.
	 * It does not do anything and AGP no longer includes annotation processors added on your project's compile classpath
	 * ```
	 */
	@Test fun `annotation processors are excluded from the classpath (debug)`() {
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.android-app")
			}
			dependencies {
				implementation "com.google.auto.service:auto-service:1.0-rc6"
			}
			// > Error while dexing. The dependency contains Java 8 bytecode.
			// > Please enable desugaring by adding the following to build.gradle
			// > See https://developer.android.com/studio/write/java8-support.html for details.
			// > Alternatively, increase the minSdkVersion to 26 or above.
			android.compileOptions.targetCompatibility = JavaVersion.VERSION_1_8
		""".trimIndent()

		val result = gradle.run(script, "assembleDebug").build()

		result.assertSuccess(":javaPreCompileDebug")
		result.assertNoOutputLine(""".*annotationProcessor.*""".toRegex())
	}

	companion object {
		private const val GOOGLE: String = DefaultRepositoryHandler.GOOGLE_REPO_NAME
		private const val MAVEN_CENTRAL: String = ArtifactRepositoryContainer.DEFAULT_MAVEN_CENTRAL_REPO_NAME
	}
}
