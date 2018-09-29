package net.twisterrob.gradle.android

import com.jakewharton.dex.DexParser
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
import org.hamcrest.junit.MatcherAssume.assumeThat
import org.intellij.lang.annotations.Language
import org.junit.Test

/**
 * @see AndroidProguardPlugin
 */
class AndroidProguardPluginIntgTest : BaseAndroidIntgTest() {

	@Test fun `default build setup minifies only release`() {
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

		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
			dependencies {
				compileOnly 'com.android.support:support-annotations:27.1.1'
			}
		""".trimIndent()

		val result = gradle.run(script, "assemble").build()

		result.assertNoTask(":transformClassesAndResourcesWithProguardForDebug")
		result.assertSuccess(":transformClassesAndResourcesWithProguardForRelease")
		result.assertSuccess(":assembleRelease")
		result.assertSuccess(":assembleDebug")
		result.assertSuccess(":assemble")
		val releaseMethods = DexParser.fromFile(gradle.root.apk("release")).listMethods()
		val debugMethods = DexParser.fromFile(gradle.root.apk("debug")).listMethods()
		val unusedMethod = dexMethod("${packageName}.SomeClass", "unusedMethod")
		val usedMethod = dexMethod("${packageName}.SomeClass", "usedMethod")
		assertThat(debugMethods, hasItems(unusedMethod, usedMethod))
		assertThat(releaseMethods, allOf(hasItem(usedMethod), not(hasItem(unusedMethod))))
	}

	@Test fun `default build setup prints ProGuard config (release)`() {
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
		""".trimIndent()

		val result = gradle.run(script, "assembleRelease").build()

		result.assertSuccess(":transformClassesAndResourcesWithProguardForRelease")
		assumeThat(gradle.root.resolve("build/outputs/mapping/release/mapping.txt"), anExistingFile())
		assertThat(gradle.root.resolve("build/outputs/mapping/release/configuration.pro"), anExistingFile())
	}

	@Test fun `extracts and uses custom ProGuard rules (release)`() {
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
		""".trimIndent()

		val result = gradle.run(script, "assembleRelease", "--info").build()

		result.assertExtractProguardRulesRunsSuccessfully()
		// com.android.build.gradle.internal.transforms.ProGuardTransform.doMinification uses LOG.info
		result.assertHasOutputLine("Applying ProGuard configuration file .*android.pro".toRegex())
		result.assertHasOutputLine("Applying ProGuard configuration file .*twisterrob.pro".toRegex())
		result.assertHasOutputLine("Applying ProGuard configuration file .*twisterrob-release.pro".toRegex())
		result.assertNoOutputLine(".*twisterrob-debug.pro.*".toRegex())
		// TODO Cannot test because BasePlugin.builtDate keeps changing
		//val incrementalResult = gradle.run(null, "extractProguardRules").build()
		//incrementalResult.assertOutcome(":extractProguardRules", TaskOutcome.UP_TO_DATE)
	}

	@Test fun `extracts and uses custom ProGuard rules (debug)`() {
		createFileToMakeSureProguardPasses()
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
			android.buildTypes.debug.minifyEnabled = true
		""".trimIndent()

		val result = gradle.run(script, "assembleDebug", "--info").build()

		result.assertExtractProguardRulesRunsSuccessfully()
		// com.android.build.gradle.internal.transforms.ProGuardTransform.doMinification uses LOG.info
		result.assertHasOutputLine("Applying ProGuard configuration file .*android.pro".toRegex())
		result.assertHasOutputLine("Applying ProGuard configuration file .*twisterrob.pro".toRegex())
		result.assertHasOutputLine("Applying ProGuard configuration file .*twisterrob-debug.pro".toRegex())
		result.assertNoOutputLine(".*twisterrob-release.pro.*".toRegex())
	}

	@Test fun `uses ProGuard files from submodules via consumerProguardFile (release)`() {
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
			dependencies {
				implementation project(':lib')
			}
		""".trimIndent()

		gradle.settingsFile().appendText("include ':lib'")

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

		// com.android.build.gradle.internal.transforms.ProGuardTransform.doMinification uses LOG.info
		result.assertHasOutputLine("Applying ProGuard configuration file .*android.pro".toRegex())
		result.assertHasOutputLine("Applying ProGuard configuration file .*twisterrob.pro".toRegex())
		result.assertHasOutputLine("Applying ProGuard configuration file .*twisterrob-release.pro".toRegex())
		result.assertNoOutputLine(".*twisterrob-debug.pro.*".toRegex())

		// check if submodule config is included
		assertThat(
			gradle.root.resolve("build/outputs/mapping/release/configuration.pro").readText(),
			containsString(dummyProguardClass)
		)
	}

	private fun BuildResult.assertExtractProguardRulesRunsSuccessfully() {
		this.assertSuccess(":extractProguardRules")
		assertThat(gradle.root.resolve("build/intermediates/proguard-rules/android.pro"), anExistingFile())
		assertThat(gradle.root.resolve("build/intermediates/proguard-rules/twisterrob.pro"), anExistingFile())
		assertThat(gradle.root.resolve("build/intermediates/proguard-rules/twisterrob-debug.pro"), anExistingFile())
		assertThat(gradle.root.resolve("build/intermediates/proguard-rules/twisterrob-release.pro"), anExistingFile())
	}
}
