package net.twisterrob.gradle.android

import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.assertNoTask
import net.twisterrob.gradle.test.assertSuccess
import net.twisterrob.gradle.test.root
import net.twisterrob.test.zip.hasZipEntry
import org.gradle.testkit.runner.BuildResult
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.not
import org.hamcrest.io.FileMatchers.anExistingFile
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junitpioneer.jupiter.ClearEnvironmentVariable
import java.io.File
import java.time.Instant
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

	@Test fun `test (release)`() {
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
		assertReleaseArchive(result)
	}

	private fun assertReleaseArchive(result: BuildResult) {
		val releasesDir = gradle.root.resolve("releases/release")
		assertArchive(releasesDir.resolve("${packageName}@10203004-v1.2.3#4+archive.zip")) { archive ->
			assertThat(archive, hasZipEntry("${packageName}@10203004-v1.2.3#4+release.apk"))
			assertThat(archive, hasZipEntry("proguard_configuration.pro"))
			assertThat(archive, hasZipEntry("proguard_dump.txt"))
			assertThat(archive, hasZipEntry("proguard_mapping.txt"))
			assertThat(archive, hasZipEntry("proguard_seeds.txt"))
			assertThat(archive, hasZipEntry("proguard_usage.txt"))
			assertThat(archive, not(hasZipEntry("output.json")))
			assertThat(archive, not(hasZipEntry("metadata.json")))
			assertThat(archive, not(hasZipEntry("output-metadata.json")))
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
			assertThat(archive, not(hasZipEntry("proguard_configuration.pro")))
			assertThat(archive, not(hasZipEntry("proguard_dump.txt")))
			assertThat(archive, not(hasZipEntry("proguard_mapping.txt")))
			assertThat(archive, not(hasZipEntry("proguard_seeds.txt")))
			assertThat(archive, not(hasZipEntry("proguard_usage.txt")))
			assertThat(archive, not(hasZipEntry("output.json")))
			assertThat(archive, not(hasZipEntry("metadata.json")))
			assertThat(archive, not(hasZipEntry("output-metadata.json")))
			result.assertHasOutputLine("Published release artifacts to ${archive.absolutePath}")
		}
	}

	@Test fun `test (debug) and (release)`() {
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
		assertReleaseArchive(result)
		assertDebugArchive(result)
	}

	private inline fun assertArchive(archive: File, crossinline assertions: (File) -> Unit) {
		assertThat(archive, anExistingFile())
		try {
			assertions(archive)
		} catch (ex: Throwable) {
			println(ZipFile(archive)
				.entries()
				.asSequence()
				.sortedBy { it.name }
				.joinToString(prefix = "'$archive' contents:\n", separator = "\n") {
					"${it.name} (${it.compressedSize}/${it.size} bytes) @ ${Instant.ofEpochMilli(it.time)}"
				})
			throw ex
		}
	}
}
