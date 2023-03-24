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
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junitpioneer.jupiter.Issue
import java.io.File

/**
 * @see AndroidMinificationPlugin
 */
@ExtendWith(GradleRunnerRuleExtension::class)
class AndroidMinificationPluginIntgTest : BaseAndroidIntgTest() {

	override lateinit var gradle: GradleRunnerRule

	@MethodSource("net.twisterrob.gradle.android.Minification#agpBasedParams")
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
			apply plugin: 'net.twisterrob.gradle.plugin.android-app'
			dependencies {
				implementation 'androidx.annotation:annotation:1.6.0'
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

	@MethodSource("net.twisterrob.gradle.android.Minification#agpBasedParams")
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
			android.useAndroidX=false
			${minification.gradleProperties}
		""".trimIndent()
		gradle.root.resolve("gradle.properties").appendText(properties)

		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.gradle.plugin.android-app'
			dependencies {
				compileOnly 'com.android.support:support-annotations:28.0.0'
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

	@MethodSource("net.twisterrob.gradle.android.Minification#agpBasedParams")
	@ParameterizedTest fun `default build setup prints ProGuard config (release)`(
		minification: Minification
	) {
		gradle.root.resolve("gradle.properties").appendText(minification.gradleProperties)

		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.gradle.plugin.android-app'
		""".trimIndent()

		val result = gradle.run(script, "assembleRelease").build()

		result.assertSuccess(":${minification.releaseTaskName}")
		assertThat(gradle.mergedProguardConfiguration("release"), anExistingFile())
	}

	@MethodSource("net.twisterrob.gradle.android.Minification#agpBasedParams")
	@ParameterizedTest fun `default build setup writes ProGuard mapping file (release)`(
		minification: Minification
	) {
		gradle.root.resolve("gradle.properties").appendText(minification.gradleProperties)

		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.gradle.plugin.android-app'
		""".trimIndent()

		val result = gradle.run(script, "assembleRelease").build()

		result.assertSuccess(":${minification.releaseTaskName}")
		assertThat(gradle.root.resolve("build/outputs/mapping/release/mapping.txt"), anExistingFile())
	}

	@MethodSource("net.twisterrob.gradle.android.Minification#agpBasedParams")
	@ParameterizedTest fun `default build setup dumps (release)`(
		minification: Minification
	) {
		gradle.root.resolve("gradle.properties").appendText(minification.gradleProperties)

		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.gradle.plugin.android-app'
		""".trimIndent()

		val result = gradle.run(script, "assembleRelease").build()

		result.assertSuccess(":${minification.releaseTaskName}")
		when (minification) {
			Minification.R8 -> {
				// Not supported on R8 at AGP 4.x.
				result.assertNoOutputLine(""".*R8: Ignoring option: -dump.*""".toRegex())
				assertThat(gradle.root.resolve("build/outputs/mapping/release/dump.txt"), not(anExistingFile()))
			}

			Minification.R8Full -> {
				// Not supported on R8 at AGP 4.x.
				result.assertNoOutputLine(""".*R8: Ignoring option: -dump.*""".toRegex())
				assertThat(gradle.root.resolve("build/outputs/mapping/release/dump.txt"), not(anExistingFile()))
			}
		}
	}

	@MethodSource("net.twisterrob.gradle.android.Minification#agpBasedParams")
	@ParameterizedTest fun `extracts and uses custom ProGuard rules (release)`(
		minification: Minification
	) {
		gradle.root.resolve("gradle.properties").appendText(minification.gradleProperties)

		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.gradle.plugin.android-app'
		""".trimIndent()

		val result = gradle.run(script, "assembleRelease", "--info").build()

		result.assertExtractMinificationRulesRunsSuccessfully()
		result.assertAppliedProguardFile(minification, "release", "android.pro")
		result.assertAppliedProguardFile(minification, "release", "twisterrob.pro")
		result.assertAppliedProguardFile(minification, "release", "twisterrob-release.pro")
		result.assertNoAppliedProguardFile(minification, "release", "twisterrob-debug.pro")
		// TODO Cannot test because BasePlugin.builtDate keeps changing
		//val incrementalResult = gradle.run(null, "extractMinificationRules").build()
		//incrementalResult.assertOutcome(":extractProguardRules", TaskOutcome.UP_TO_DATE)
	}

	@MethodSource("net.twisterrob.gradle.android.Minification#agpBasedParams")
	@ParameterizedTest fun `extracts and uses custom ProGuard rules (debug)`(
		minification: Minification
	) {
		gradle.root.resolve("gradle.properties").appendText(minification.gradleProperties)

		createFileToMakeSureProguardPasses()
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.gradle.plugin.android-app'
			android.buildTypes.debug.minifyEnabled = true
		""".trimIndent()

		val result = gradle.run(script, "assembleDebug", "--info").build()

		result.assertExtractMinificationRulesRunsSuccessfully()
		result.assertAppliedProguardFile(minification, "debug", "android.pro")
		result.assertAppliedProguardFile(minification, "debug", "twisterrob.pro")
		result.assertAppliedProguardFile(minification, "debug", "twisterrob-debug.pro")
		result.assertNoAppliedProguardFile(minification, "debug", "twisterrob-release.pro")
	}

	@MethodSource("net.twisterrob.gradle.android.Minification#agpBasedParams")
	@ParameterizedTest fun `uses ProGuard files from submodules via consumerProguardFile (release)`(
		minification: Minification
	) {
		gradle.root.resolve("gradle.properties").appendText(minification.gradleProperties)

		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.gradle.plugin.android-app'
			dependencies {
				implementation project(':lib')
			}
		""".trimIndent()

		gradle.settingsFile.appendText("include ':lib'")

		@Language("gradle")
		val libGradle = """
			apply plugin: 'net.twisterrob.gradle.plugin.android-library'
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

		result.assertAppliedProguardFile(minification, "release", "android.pro")
		result.assertAppliedProguardFile(minification, "release", "twisterrob.pro")
		result.assertAppliedProguardFile(minification, "release", "twisterrob-release.pro")
		result.assertNoAppliedProguardFile(minification, "release", "twisterrob-debug.pro")

		// check if submodule config is included
		assertThat(gradle.mergedProguardConfiguration("release").readText(), containsString(dummyProguardClass))
	}

	@Issue("https://github.com/TWiStErRob/net.twisterrob.gradle/issues/214")
	@Test fun `extract task wires with Android Lint correctly`() {
		gradle.settingsFile.appendText("include ':lib'")

		@Language("gradle")
		val libGradle = """
			apply plugin: 'net.twisterrob.gradle.plugin.android-library'
		""".trimIndent()
		gradle.file(libGradle, "lib", "build.gradle")

		@Language("xml")
		val libManifest = """
			<manifest package="${packageName}.lib" />
		""".trimIndent()
		gradle.file(libManifest, "lib", "src", "main", "AndroidManifest.xml")

		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.gradle.plugin.android-app'
			dependencies { implementation project(':lib') }
			android.lintOptions.checkDependencies = true
			tasks.named("lint") { dependsOn("lintRelease") } // By default this is not the case in AGP 7.x.
		""".trimIndent()
		val result = gradle.run(script, "extractMinificationRules", "build").build()

		result.assertSuccess(":extractMinificationRules")
		result.assertSuccess(":lint")
	}

	private fun BuildResult.assertExtractMinificationRulesRunsSuccessfully() {
		this.assertSuccess(":extractMinificationRules")
		assertThat(gradle.root.resolve("build/intermediates/proguard-rules/android.pro"), anExistingFile())
		assertThat(gradle.root.resolve("build/intermediates/proguard-rules/twisterrob.pro"), anExistingFile())
		assertThat(gradle.root.resolve("build/intermediates/proguard-rules/twisterrob-debug.pro"), anExistingFile())
		assertThat(gradle.root.resolve("build/intermediates/proguard-rules/twisterrob-release.pro"), anExistingFile())
	}

	private fun BuildResult.assertAppliedProguardFile(minification: Minification, variant: String, fileName: String) {
		when (minification) {
			Minification.R8,
			Minification.R8Full -> {
				val configFile = gradle.root.resolve("build/intermediates/proguard-rules/${fileName}")
				assertThat(
					gradle.mergedProguardConfiguration(variant).readText(),
					containsString("# The proguard configuration file for the following section is ${configFile.absolutePath}")
				)
				assertThat(
					gradle.mergedProguardConfiguration(variant).readText(),
					containsString("### -- twister-plugin-gradle/${fileName} -- ###")
				)
				assertThat(
					gradle.mergedProguardConfiguration(variant).readText(),
					containsString("# End of content from ${configFile.absolutePath}")
				)
			}
		}
	}

	private fun BuildResult.assertNoAppliedProguardFile(minification: Minification, variant: String, fileName: String) {
		when (minification) {
			Minification.R8,
			Minification.R8Full -> {
				assertThat(gradle.mergedProguardConfiguration(variant).readText(), not(containsString(fileName)))
			}
		}
	}
}

private fun GradleRunnerRule.mergedProguardConfiguration(variant: String): File =
	this.root.resolve("build/outputs/mapping/${variant}/configuration.txt")
