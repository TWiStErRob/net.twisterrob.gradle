package net.twisterrob.gradle.android

import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.assertSuccess
import net.twisterrob.gradle.test.root
import net.twisterrob.test.zip.hasZipEntry
import net.twisterrob.test.zip.withSize
import org.hamcrest.Matchers.greaterThan
import org.hamcrest.io.FileMatchers.anExistingFile
import org.hamcrest.junit.MatcherAssert.assertThat
import org.intellij.lang.annotations.Language
import org.junit.Test
import java.time.Instant
import java.util.zip.ZipFile

/**
 * @see AndroidReleasePlugin
 */
class AndroidReleasePluginIntgTest : BaseAndroidIntgTest() {

	@Test fun `test (release)`() {
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
			android.defaultConfig.version { major = 1; minor = 2; patch = 3; build = 4 }
			afterEvaluate {
				// TODO workaround for testing until a Task input property is introduced instead of env.RELEASE_HOME
				tasks.releaseRelease.destinationDir = file('releases')
			}
		""".trimIndent()

		val result = gradle.run(script, "releaseRelease").build()

		result.assertSuccess(":assembleRelease")
		result.assertSuccess(":releaseRelease")
		val releasesDir = gradle.root.resolve("releases")
		val archive = releasesDir.resolve("${packageName}@10203004-v1.2.3#4+archive.zip")
		assertThat(archive, anExistingFile())
		try {
			assertThat(archive, hasZipEntry("${packageName}@10203004-v1.2.3#4+release.apk", withSize(greaterThan(0L))))
			assertThat(archive, hasZipEntry("proguard_configuration.pro", withSize(greaterThan(0L))))
			assertThat(archive, hasZipEntry("proguard_dump.txt", withSize(greaterThan(0L))))
			assertThat(archive, hasZipEntry("proguard_mapping.txt", withSize(greaterThan(0L))))
			assertThat(archive, hasZipEntry("proguard_seeds.txt", withSize(greaterThan(0L))))
			assertThat(archive, hasZipEntry("proguard_usage.txt", withSize(greaterThan(0L))))
		} catch (ex: Throwable) {
			println(ZipFile(archive)
				.entries()
				.asSequence()
				.sortedBy { it.name }
				.joinToString("\n") {
					"${it.name} (${it.compressedSize}/${it.size} bytes) @ ${Instant.ofEpochMilli(
						it.time
					)}"
				})
			throw ex
		}
		result.assertHasOutputLine("Published release artifacts to ${archive.absolutePath}")
	}
}
