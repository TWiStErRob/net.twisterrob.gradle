package net.twisterrob.gradle.android

import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertFailed
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.assertNoOutputLine
import net.twisterrob.gradle.test.assertNoTask
import net.twisterrob.gradle.test.assertSuccess
import net.twisterrob.gradle.test.fixtures.ContentMergeMode
import net.twisterrob.gradle.test.root
import net.twisterrob.test.withRootCause
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
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junitpioneer.jupiter.ClearEnvironmentVariable
import java.io.File
import java.io.IOException
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
			plugins {
				id("net.twisterrob.gradle.plugin.android-app")
			}
		""".trimIndent()

		val result = gradle.run(script, "assemble", "assembleAndroidTest").build()

		result.assertSuccess(":assembleDebug")
		result.assertSuccess(":assembleRelease")
		result.assertSuccess(":assembleDebugAndroidTest")
		result.assertNoTask(":releaseDebug")
		result.assertNoTask(":releaseRelease")
		result.assertNoTask(":releaseAll")
	}

	@Test fun `releases location can be overridden from DSL (debug)`(@TempDir externalReleaseDir: File) {
		val externalReleaseDirPath = externalReleaseDir.absolutePath.replace("\\", "/")
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.android-app")
			}
			android.defaultConfig.version { major = 1; minor = 2; patch = 3; build = 4 }
			android.release.directory.fileValue(file("${externalReleaseDirPath}"))
		""".trimIndent()

		val result = gradle.run(script, "releaseDebug").build()

		result.assertDebugArchive(externalReleaseDir)
	}

	@Test fun `releases location can be overridden from property (debug)`(@TempDir externalReleaseDir: File) {
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.android-app")
			}
			android.defaultConfig.version { major = 1; minor = 2; patch = 3; build = 4 }
		""".trimIndent()

		val externalReleaseDirPath = externalReleaseDir.absolutePath.replace("\\", "/")
		@Language("properties")
		val properties = """
			net.twisterrob.android.release.directory=${externalReleaseDirPath}
		""".trimIndent()
		gradle.file(properties, ContentMergeMode.APPEND, "gradle.properties")

		val result = gradle.run(script, "releaseDebug").build()

		result.assertDebugArchive(externalReleaseDir)
	}

	@Test fun `releases location can be set to a relative path (debug)`() {
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.android-app")
			}
			android.defaultConfig.version { major = 1; minor = 2; patch = 3; build = 4 }
		""".trimIndent()

		@Language("properties")
		val properties = """
			net.twisterrob.android.release.directory=my/releases
		""".trimIndent()
		gradle.file(properties, ContentMergeMode.APPEND, "gradle.properties")

		val result = gradle.run(script, "releaseDebug").build()

		result.assertDebugArchive(File(gradle.root, "my/releases"))
	}

	@MethodSource("net.twisterrob.gradle.android.Minification#agpBasedParams")
	@ParameterizedTest fun `releases the app to default location (release)`(
		minification: Minification
	) {
		gradle.root.resolve("gradle.properties").appendText(minification.gradleProperties)

		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.android-app")
			}
			android.defaultConfig.version { major = 1; minor = 2; patch = 3; build = 4 }
		""".trimIndent()

		val result = gradle.run(script, "releaseRelease").build()

		result.assertNoTask(":release")
		result.assertNoTask(":packageDebug")
		result.assertNoTask(":releaseDebug")
		result.assertSuccess(":packageRelease")
		result.assertSuccess(":releaseRelease")
		result.assertReleaseArchive(gradle.root.resolve("build/release"), minification)
	}

	private fun BuildResult.assertReleaseArchive(releasesDir: File, minification: Minification) {
		assertArchive(releasesDir.resolve("${packageName}@10203004-v1.2.3#4+archive.zip")) { archive ->
			assertThat(archive, hasZipEntry("${packageName}@10203004-v1.2.3#4+release.apk"))
			assertThat(archive, hasZipEntry("proguard_configuration.txt"))
			when (minification) {
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
			assertThat(archive, hasEntryCount(equalTo(5)))
			this.assertHasOutputLine("Published release artifacts to ${archive.absolutePath}:")
		}
	}

	@Test fun `releases the app to default location (debug)`() {
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.android-app")
			}
			android.defaultConfig.version { major = 1; minor = 2; patch = 3; build = 4 }
		""".trimIndent()

		val result = gradle.run(script, "releaseDebug").build()

		result.assertNoTask(":release")
		result.assertNoTask(":packageRelease")
		result.assertNoTask(":releaseRelease")
		result.assertSuccess(":packageDebug")
		result.assertSuccess(":packageDebugAndroidTest")
		result.assertSuccess(":releaseDebug")
		result.assertDebugArchive(gradle.root.resolve("build/release"))
	}

	private fun BuildResult.assertDebugArchive(releasesDir: File) {
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
			this.assertHasOutputLine("Published release artifacts to ${archive.absolutePath}:")
		}
	}

	@MethodSource("net.twisterrob.gradle.android.Minification#agpBasedParams")
	@ParameterizedTest fun `release location for each variant and release everything at once (debug) and (release)`(
		minification: Minification
	) {
		gradle.root.resolve("gradle.properties").appendText(minification.gradleProperties)

		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.android-app")
			}
			android.defaultConfig.version { major = 1; minor = 2; patch = 3; build = 4 }
			afterEvaluate {
				tasks.named("releaseRelease", Zip) { destinationDirectory.set(file('releases/release')) }
				tasks.named("releaseDebug", Zip) { destinationDirectory.set(file('releases/debug')) }
			}
		""".trimIndent()

		val result = gradle.run(script, "release").build()

		result.assertSuccess(":packageRelease")
		result.assertSuccess(":packageDebug")
		result.assertSuccess(":packageDebugAndroidTest")
		result.assertSuccess(":releaseRelease")
		result.assertSuccess(":releaseDebug")
		result.assertReleaseArchive(gradle.root.resolve("releases/release"), minification)
		result.assertDebugArchive(gradle.root.resolve("releases/debug"))
	}

	@Test fun `test app fails to release when already exists (debug) and (release)`() {
		gradle.root.resolve("gradle.properties").appendText(Minification.R8.gradleProperties)

		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.android-app")
			}
			android.defaultConfig.version { major = 1; minor = 2; patch = 3; build = 4 }
		""".trimIndent()

		val setup = gradle.run(script, "release").build()
		setup.assertSuccess(":releaseRelease")
		setup.assertSuccess(":releaseDebug")

		// Make a change to force non-up-to-date packageRelease.
		gradle.root.resolve("gradle.properties").appendText("\n")
		gradle.root.resolve("gradle.properties").appendText(Minification.R8Full.gradleProperties)

		val result = gradle.run(null, "releaseRelease").buildAndFail()
		result.assertFailed(":releaseRelease")
		result.assertHasOutputLine(Regex(""".*Target zip file already exists\..*"""))
		result.assertHasOutputLine(Regex(""".*Release archive:.*\+archive\.zip"""))
		result.assertNoOutputLine(Regex(""".*Published release artifacts.*"""))
	}

	private inline fun assertArchive(archive: File, crossinline assertions: (File) -> Unit) {
		try {
			assertThat(archive.absolutePath, archive, anExistingFile())
		} catch (@Suppress("SwallowedException") ex: Throwable) { // Detekt doesn't see into withRootCause.
			val contents = archive
				.parentFile
				.listFiles()
				.orEmpty()
				.joinToString(prefix = "'${archive.parentFile}' contents:\n", separator = "\n")
			throw ex.withRootCause(IOException(contents))
		}
		try {
			assertions(archive)
		} catch (@Suppress("SwallowedException") ex: Throwable) { // Detekt doesn't see into withRootCause.
			val contents = ZipFile(archive).use { zip ->
				zip
					.entries()
					.asSequence()
					.sortedBy { it.name }
					.joinToString(prefix = "'$archive' contents:\n", separator = "\n") {
						"${it.name} (${it.compressedSize}/${it.size} bytes) @ ${Instant.ofEpochMilli(it.time)}"
					}
			}
			throw ex.withRootCause(ZipException(contents))
		}
	}
}
