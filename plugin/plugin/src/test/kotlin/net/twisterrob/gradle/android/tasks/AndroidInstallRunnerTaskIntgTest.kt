package net.twisterrob.gradle.android.tasks

import net.twisterrob.gradle.android.AndroidBuildPlugin
import net.twisterrob.gradle.android.BaseAndroidIntgTest
import net.twisterrob.gradle.android.assertFailed
import net.twisterrob.gradle.android.assertNoTask
import net.twisterrob.gradle.test.assertHasOutputLine
import org.intellij.lang.annotations.Language
import org.junit.Test

/**
 * [AndroidInstallRunnerTask] via [AndroidBuildPlugin]
 * @see AndroidInstallRunnerTask
 * @see AndroidBuildPlugin
 */
class AndroidInstallRunnerTaskIntgTest : BaseAndroidIntgTest() {

	@Test fun `adds run tasks (debug)`() {
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
			//android.twisterrob.addRunTasks = true // default
			afterEvaluate {
				tasks.runDebug.dependsOn -= tasks.installDebug
				tasks.runDebug.dependsOn tasks.assembleDebug
			}
		""".trimIndent()

		val result = gradle.run(script, "runDebug").buildAndFail()

		// TODO should be assertSuccess, but relies on emulator/device being connected
		result.assertFailed(":runDebug")
		// line is output to stderr, so no control over being on a new line
		result.assertHasOutputLine(""".*error: no devices/emulators found.*""".toRegex())
	}

	@Test fun `don't add run tasks (debug)`() {
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
			android.twisterrob.addRunTasks = false
		""".trimIndent()

		val result = gradle.run(script, "runDebug").buildAndFail()

		result.assertNoTask(":runDebug")
		result.assertHasOutputLine("""Task 'runDebug' not found in root project '.*?'\..*""".toRegex())
	}

	@Test fun `don't add run tasks (release)`() {
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
			android.twisterrob.addRunTasks = false
		""".trimIndent()

		val result = gradle.run(script, "runRelease").buildAndFail()

		result.assertNoTask(":runRelease")
		result.assertHasOutputLine("""Task 'runRelease' not found in root project '.*?'\..*""".toRegex())
	}
}
