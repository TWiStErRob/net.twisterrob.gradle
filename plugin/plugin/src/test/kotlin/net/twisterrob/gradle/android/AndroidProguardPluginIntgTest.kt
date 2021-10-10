package net.twisterrob.gradle.android

import com.jakewharton.dex.DexParser.Companion.toDexParser
import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.assertNoOutputLine
import net.twisterrob.gradle.test.assertNoTask
import net.twisterrob.gradle.test.assertSuccess
import net.twisterrob.gradle.test.root
import org.gradle.testkit.runner.BuildResult
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.hasItems
import org.hamcrest.Matchers.not
import org.hamcrest.io.FileMatchers.anExistingFile
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

/**
 * @see AndroidProguardPlugin
 */
@ExtendWith(GradleRunnerRuleExtension::class)
class AndroidProguardPluginIntgTest : BaseAndroidIntgTest() {

	enum class Minification(
		val debugTaskName: String,
		val releaseTaskName: String,
		val gradleProperties: String,
	) {

		ProGuard(
			debugTaskName = "minifyDebugWithProguard",
			releaseTaskName = "minifyReleaseWithProguard",
			gradleProperties = """
				android.enableR8=false
			""".trimIndent()
		),
		R8(
			debugTaskName = "minifyDebugWithR8",
			releaseTaskName = "minifyReleaseWithR8",
			gradleProperties = """
				android.enableR8=true
			""".trimIndent()
		),
	}

	override lateinit var gradle: GradleRunnerRule

	@EnumSource(Minification::class)
	@ParameterizedTest fun `default build setup minifies only release using AndroidX (debug) and (release)`(
		minification: Minification
	) {
		@Language("java")
		val someClass = """
			package ${packageName};
			public class SomeClass {
				@androidx.annotation.Keep
				public void usedMethod() { }
				// no reason to -keep it, it'll be optimized away
				public void unusedMethod() { }
			}
		""".trimIndent()
		gradle.file(someClass, "src/main/java/${packageFolder}/SomeClass.java")

		@Language("properties")
		val properties = """
			android.useAndroidX=true
			${minification.gradleProperties}
		""".trimIndent()
		gradle.root.resolve("gradle.properties").appendText(properties)

		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
			dependencies {
				implementation 'androidx.annotation:annotation:1.1.0'
			}
		""".trimIndent()

		val result = gradle.run(script, "assemble").build()

		result.assertNoTask(":${minification.debugTaskName}")
		result.assertSuccess(":${minification.releaseTaskName}")
		result.assertSuccess(":assembleRelease")
		result.assertSuccess(":assembleDebug")
		result.assertSuccess(":assemble")
		val releaseMethods = gradle.root.apk("release").toDexParser().listMethods()
		val debugMethods = gradle.root.apk("debug").toDexParser().listMethods()
		val unusedMethod = dexMethod("${packageName}.SomeClass", "unusedMethod")
		val usedMethod = dexMethod("${packageName}.SomeClass", "usedMethod")
		assertThat(debugMethods, hasItems(unusedMethod, usedMethod))
		assertThat(releaseMethods, allOf(hasItem(usedMethod), not(hasItem(unusedMethod))))
	}

	@EnumSource(Minification::class)
	@ParameterizedTest fun `default build setup minifies only release using support library (debug) and (release)`(
		minification: Minification
	) {
		@Language("java")
		val someClass = """
			package ${packageName};
			public class SomeClass {
				@android.support.annotation.Keep
				public void usedMethod() { }
				// no reason to -keep it, it'll be optimized away
				public void unusedMethod() { }
			}
		""".trimIndent()
		gradle.file(someClass, "src/main/java/${packageFolder}/SomeClass.java")

		@Language("properties")
		val properties = """
			android.useAndroidX=true
			${minification.gradleProperties}
		""".trimIndent()
		gradle.root.resolve("gradle.properties").appendText(properties)

		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
			dependencies {
				compileOnly 'com.android.support:support-annotations:27.1.1'
			}
		""".trimIndent()

		val result = gradle.run(script, "assemble").build()

		result.assertNoTask(":${minification.debugTaskName}")
		result.assertSuccess(":${minification.releaseTaskName}")
		result.assertSuccess(":assembleRelease")
		result.assertSuccess(":assembleDebug")
		result.assertSuccess(":assemble")
		val releaseMethods = gradle.root.apk("release").toDexParser().listMethods()
		val debugMethods = gradle.root.apk("debug").toDexParser().listMethods()
		val unusedMethod = dexMethod("${packageName}.SomeClass", "unusedMethod")
		val usedMethod = dexMethod("${packageName}.SomeClass", "usedMethod")
		assertThat(debugMethods, hasItems(unusedMethod, usedMethod))
		assertThat(releaseMethods, allOf(hasItem(usedMethod), not(hasItem(unusedMethod))))
	}

	@EnumSource(Minification::class)
	@ParameterizedTest fun `default build setup prints ProGuard config (release)`(
		minification: Minification
	) {
		gradle.root.resolve("gradle.properties").appendText(minification.gradleProperties)

		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
		""".trimIndent()

		val result = gradle.run(script, "assembleRelease").build()

		result.assertSuccess(":${minification.releaseTaskName}")
		assertThat(gradle.root.resolve("build/outputs/mapping/release/configuration.pro"), anExistingFile())
	}

	@EnumSource(Minification::class)
	@ParameterizedTest fun `default build setup writes ProGuard mapping file (release)`(
		minification: Minification
	) {
		gradle.root.resolve("gradle.properties").appendText(minification.gradleProperties)

		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
		""".trimIndent()

		val result = gradle.run(script, "assembleRelease").build()

		result.assertSuccess(":${minification.releaseTaskName}")
		assertThat(gradle.root.resolve("build/outputs/mapping/release/mapping.txt"), anExistingFile())
	}

	@EnumSource(Minification::class)
	@ParameterizedTest fun `default build setup dumps (release)`(
		minification: Minification
	) {
		gradle.root.resolve("gradle.properties").appendText(minification.gradleProperties)

		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
		""".trimIndent()

		val result = gradle.run(script, "assembleRelease").build()

		result.assertSuccess(":${minification.releaseTaskName}")
		assertThat(gradle.root.resolve("build/outputs/mapping/release/dump.txt"), anExistingFile())
	}

	@EnumSource(Minification::class)
	@ParameterizedTest fun `extracts and uses custom ProGuard rules (release)`(
		minification: Minification
	) {
		gradle.root.resolve("gradle.properties").appendText(minification.gradleProperties)

		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
		""".trimIndent()

		val result = gradle.run(script, "assembleRelease", "--info").build()

		result.assertExtractMinificationRulesRunsSuccessfully()
		when (minification) {
			Minification.ProGuard -> {
				// com.android.build.gradle.internal.transforms.ProGuardTransform.doMinification uses LOG.info
				result.assertHasOutputLine("Applying ProGuard configuration file .*android.pro".toRegex())
				result.assertHasOutputLine("Applying ProGuard configuration file .*twisterrob.pro".toRegex())
				result.assertHasOutputLine("Applying ProGuard configuration file .*twisterrob-release.pro".toRegex())
				result.assertNoOutputLine(".*twisterrob-debug.pro.*".toRegex())
			}

			Minification.R8 -> {
				// STOPSHIP
			}
		}
		// TODO Cannot test because BasePlugin.builtDate keeps changing
		//val incrementalResult = gradle.run(null, "extractMinificationRules").build()
		//incrementalResult.assertOutcome(":extractProguardRules", TaskOutcome.UP_TO_DATE)
	}

	@EnumSource(Minification::class)
	@ParameterizedTest fun `extracts and uses custom ProGuard rules (debug)`(
		minification: Minification
	) {
		gradle.root.resolve("gradle.properties").appendText(minification.gradleProperties)

		createFileToMakeSureProguardPasses()
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
			android.buildTypes.debug.minifyEnabled = true
		""".trimIndent()

		val result = gradle.run(script, "assembleDebug", "--info").build()

		result.assertExtractMinificationRulesRunsSuccessfully()
		when (minification) {
			Minification.ProGuard -> {
				// com.android.build.gradle.internal.transforms.ProGuardTransform.doMinification uses LOG.info
				result.assertHasOutputLine("Applying ProGuard configuration file .*android.pro".toRegex())
				result.assertHasOutputLine("Applying ProGuard configuration file .*twisterrob.pro".toRegex())
				result.assertHasOutputLine("Applying ProGuard configuration file .*twisterrob-debug.pro".toRegex())
				result.assertNoOutputLine(".*twisterrob-release.pro.*".toRegex())
			}

			Minification.R8 -> {
				// STOPSHIP
			}
		}
	}

	@EnumSource(Minification::class)
	@ParameterizedTest fun `uses ProGuard files from submodules via consumerProguardFile (release)`(
		minification: Minification
	) {
		gradle.root.resolve("gradle.properties").appendText(minification.gradleProperties)

		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
			dependencies {
				implementation project(':lib')
			}
		""".trimIndent()

		gradle.settingsFile.appendText("include ':lib'")

		@Language("gradle")
		val libGradle = """
			apply plugin: 'net.twisterrob.android-library'
			android.defaultConfig.consumerProguardFile 'proguard.pro'
		""".trimIndent()
		gradle.file(libGradle, "lib", "build.gradle")
		@Language("xml")
		val androidManifest = """
			<manifest package="${packageName}.lib" />
		""".trimIndent()
		gradle.file(androidManifest, "lib", "src", "main", "AndroidManifest.xml")
		val dummyProguardClass = "some.dummy.thing.SoItShowsUpInTheMergeConfiguration"

		@Language("proguard")
		val libProguardFile = """
			# library proguard file for test
			-dontwarn ${dummyProguardClass}
		""".trimIndent()
		gradle.file(libProguardFile, "lib", "proguard.pro")

		val result = gradle.run(script, "assembleRelease", "--info").build()

		when (minification) {
			Minification.ProGuard -> {
				// com.android.build.gradle.internal.transforms.ProGuardTransform.doMinification uses LOG.info
				result.assertHasOutputLine("Applying ProGuard configuration file .*android.pro".toRegex())
				result.assertHasOutputLine("Applying ProGuard configuration file .*twisterrob.pro".toRegex())
				result.assertHasOutputLine("Applying ProGuard configuration file .*twisterrob-release.pro".toRegex())
				result.assertNoOutputLine(".*twisterrob-debug.pro.*".toRegex())
			}

			Minification.R8 -> {
				// STOPSHIP
			}
		}

		// check if submodule config is included
		assertThat(
			gradle.root.resolve("build/outputs/mapping/release/configuration.pro").readText(),
			containsString(dummyProguardClass)
		)
	}

	private fun BuildResult.assertExtractMinificationRulesRunsSuccessfully() {
		this.assertSuccess(":extractMinificationRules")
		assertThat(gradle.root.resolve("build/intermediates/proguard-rules/android.pro"), anExistingFile())
		assertThat(gradle.root.resolve("build/intermediates/proguard-rules/twisterrob.pro"), anExistingFile())
		assertThat(gradle.root.resolve("build/intermediates/proguard-rules/twisterrob-debug.pro"), anExistingFile())
		assertThat(gradle.root.resolve("build/intermediates/proguard-rules/twisterrob-release.pro"), anExistingFile())
	}
}
