package net.twisterrob.gradle.android

import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.assertNoTask
import net.twisterrob.gradle.test.assertSuccess
import net.twisterrob.gradle.test.root
import net.twisterrob.test.zip.hasEntryCount
import net.twisterrob.test.zip.hasZipEntry
import net.twisterrob.test.zip.withSize
import org.gradle.testkit.runner.BuildResult
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.hamcrest.Matchers.not
import org.hamcrest.io.FileMatchers.anExistingFile
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junitpioneer.jupiter.ClearEnvironmentVariable
import java.io.File
import java.time.Instant
import java.util.zip.ZipException
import java.util.zip.ZipFile

/**
 * @see AndroidReleasePlugin
 */
@ExtendWith(GradleRunnerRuleExtension::class)
@ClearEnvironmentVariable(key = "RELEASE_HOME")
class AndroidReleasePluginIntgTest : BaseAndroidIntgTest() {

	override lateinit var gradle: GradleRunnerRule

	@Test fun `assemble doesn't need env var`() {
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
		""".trimIndent()

		val result = gradle.run(script, "assemble", "assembleAndroidTest").build()

		result.assertSuccess(":assembleDebug")
		result.assertSuccess(":assembleRelease")
		result.assertSuccess(":assembleDebugAndroidTest")
		result.assertNoTask(":releaseDebug")
		result.assertNoTask(":releaseRelease")
		result.assertNoTask(":releaseAll")
	}

	@Test fun `needs environment variable to release (debug)`() {
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
		""".trimIndent()

		val result = gradle.run(script, "releaseDebug").buildAndFail()

		result.assertHasOutputLine(""".*Please set RELEASE_HOME environment variable to an existing directory\.""".toRegex())
	}

	@Test fun `needs environment variable to release (release)`() {
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
		""".trimIndent()

		val result = gradle.run(script, "releaseRelease").buildAndFail()

		result.assertHasOutputLine(""".*Please set RELEASE_HOME environment variable to an existing directory\.""".toRegex())
	}

	@MethodSource("net.twisterrob.gradle.android.Minification#agpBasedParams")
	@ParameterizedTest fun `test (release)`(
		minification: Minification
	) {
		gradle.root.resolve("gradle.properties").appendText(minification.gradleProperties)

		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
			android.defaultConfig.version { major = 1; minor = 2; patch = 3; build = 4 }
			afterEvaluate {
				tasks.named("releaseRelease", Zip) { destinationDirectory.set(file('releases/release')) }
			}
		""".trimIndent()

		val result = gradle.run(script, "releaseRelease").build()

		result.assertSuccess(":assembleRelease")
		result.assertSuccess(":releaseRelease")
		assertReleaseArchive(result, minification)
	}

	private fun assertReleaseArchive(result: BuildResult, minification: Minification) {
		val releasesDir = gradle.root.resolve("releases/release")
		assertArchive(releasesDir.resolve("${packageName}@10203004-v1.2.3#4+archive.zip")) { archive ->
			assertThat(archive, hasZipEntry("${packageName}@10203004-v1.2.3#4+release.apk"))
			assertThat(archive, hasZipEntry("proguard_configuration.txt"))
			when (minification) {
				Minification.ProGuard -> {
					assertThat(archive, hasZipEntry("proguard_dump.txt"))
					assertThat(archive, hasZipEntry("proguard_mapping.txt"))
				}
				Minification.R8 -> {
					assertThat(archive, hasZipEntry("proguard_mapping.txt"))
				}
				Minification.R8Full -> {
					// TODO for some reason the full R8 doesn't output the mapping. Probably because nothing was renamed.
					assertThat(archive, hasZipEntry("proguard_mapping.txt", withSize(greaterThanOrEqualTo(0L))))
				}
			}
			assertThat(archive, hasZipEntry("proguard_seeds.txt"))
			assertThat(archive, hasZipEntry("proguard_usage.txt"))
			assertThat(archive, not(hasZipEntry("output.json")))
			assertThat(archive, not(hasZipEntry("metadata.json")))
			assertThat(archive, not(hasZipEntry("output-metadata.json")))
			assertThat(archive, hasEntryCount(equalTo(if (minification == Minification.ProGuard) 6 else 5)))
			result.assertHasOutputLine("Published release artifacts to ${archive.absolutePath}")
		}
	}

	@Test fun `test (debug)`() {
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
			android.defaultConfig.version { major = 1; minor = 2; patch = 3; build = 4 }
			afterEvaluate {
				tasks.named("releaseDebug", Zip) { destinationDirectory.set(file('releases/debug')) }
			}
		""".trimIndent()

		val result = gradle.run(script, "releaseDebug").build()

		result.assertSuccess(":assembleDebug")
		result.assertSuccess(":assembleDebugAndroidTest")
		result.assertSuccess(":releaseDebug")
		assertDebugArchive(result)
	}

	private fun assertDebugArchive(result: BuildResult) {
		val releasesDir = gradle.root.resolve("releases/debug")
		assertArchive(releasesDir.resolve("${packageName}.debug@10203004-v1.2.3#4d+archive.zip")) { archive ->
			assertThat(archive, hasZipEntry("${packageName}.debug@10203004-v1.2.3#4d+debug.apk"))
			assertThat(archive, hasZipEntry("${packageName}.debug.test@10203004-v1.2.3#4d+debug-androidTest.apk"))
			assertThat(archive, not(hasZipEntry("proguard_configuration.txt")))
			assertThat(archive, not(hasZipEntry("proguard_dump.txt")))
			assertThat(archive, not(hasZipEntry("proguard_mapping.txt")))
			assertThat(archive, not(hasZipEntry("proguard_seeds.txt")))
			assertThat(archive, not(hasZipEntry("proguard_usage.txt")))
			assertThat(archive, not(hasZipEntry("output.json")))
			assertThat(archive, not(hasZipEntry("metadata.json")))
			assertThat(archive, not(hasZipEntry("output-metadata.json")))
			assertThat(archive, hasEntryCount(equalTo(2)))
			result.assertHasOutputLine("Published release artifacts to ${archive.absolutePath}")
		}
	}

	@MethodSource("net.twisterrob.gradle.android.Minification#agpBasedParams")
	@ParameterizedTest fun `test (debug) and (release)`(
		minification: Minification
	) {
		gradle.root.resolve("gradle.properties").appendText(minification.gradleProperties)

		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
			android.defaultConfig.version { major = 1; minor = 2; patch = 3; build = 4 }
			afterEvaluate {
				tasks.named("releaseRelease", Zip) { destinationDirectory.set(file('releases/release')) }
				tasks.named("releaseDebug", Zip) { destinationDirectory.set(file('releases/debug')) }
			}
		""".trimIndent()

		val result = gradle.run(script, "release").build()

		result.assertSuccess(":assembleRelease")
		result.assertSuccess(":assembleDebug")
		result.assertSuccess(":assembleDebugAndroidTest")
		result.assertSuccess(":releaseRelease")
		result.assertSuccess(":releaseDebug")
		assertReleaseArchive(result, minification)
		assertDebugArchive(result)
	}

	private inline fun assertArchive(archive: File, crossinline assertions: (File) -> Unit) {
		val fileNamesMessage =
			"Wanted: ${archive.absolutePath}${System.lineSeparator()}list: ${
				archive.parentFile.listFiles().orEmpty().joinToString(
					prefix = System.lineSeparator(),
					separator = System.lineSeparator()
				)
			}"
		assertThat(fileNamesMessage, archive, anExistingFile())
		try {
			assertions(archive)
		} catch (ex: Throwable) {
			val contents = ZipFile(archive)
				.entries()
				.asSequence()
				.sortedBy { it.name }
				.joinToString(prefix = "'$archive' contents:\n", separator = "\n") {
					"${it.name} (${it.compressedSize}/${it.size} bytes) @ ${Instant.ofEpochMilli(it.time)}"
				}
			generateSequence(ex) { ex.cause }.last().initCause(ZipException(contents))
			throw ex
		}
	}
}
