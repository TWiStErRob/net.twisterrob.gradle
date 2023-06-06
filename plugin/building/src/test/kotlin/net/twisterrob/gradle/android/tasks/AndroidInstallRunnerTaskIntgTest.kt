package net.twisterrob.gradle.android.tasks

import net.twisterrob.gradle.android.AndroidBuildPlugin
import net.twisterrob.gradle.android.BaseAndroidIntgTest
import net.twisterrob.gradle.android.hasDevices
import net.twisterrob.gradle.android.packageName
import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertFailed
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.assertNoOutputLine
import net.twisterrob.gradle.test.assertSkipped
import net.twisterrob.gradle.test.assertSuccess
import net.twisterrob.gradle.test.delete
import org.gradle.api.JavaVersion
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Test [AndroidInstallRunnerTask] via [AndroidBuildPlugin].
 *
 * @see AndroidInstallRunnerTask
 * @see AndroidBuildPlugin
 */
@ExtendWith(GradleRunnerRuleExtension::class)
class AndroidInstallRunnerTaskIntgTest : BaseAndroidIntgTest() {

	override lateinit var gradle: GradleRunnerRule

	@Test fun `adds run task, or installs and runs activity (debug)`() {
		// Having two paths in tests in not nice, but flaky tests are even worse.
		// Two @Test methods with assumeTrue|False would work, but then there's always an ignored test.

		// Note: only detect devices on Java 17 because https://github.com/TWiStErRob/net.twisterrob.gradle/issues/380.
		// This will cause a false negative when test is run Java 11 with an emulator running, but that's acceptable.
		val hasDevices = JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_17) && hasDevices()

		@Language("xml")
		val androidManifest = """
			<manifest xmlns:android="http://schemas.android.com/apk/res/android">
				<application>
					<activity android:name="${packageName}.MainActivity" android:exported="true">
						<intent-filter>
							<action android:name="android.intent.action.MAIN" />
							<category android:name="android.intent.category.LAUNCHER" />
						</intent-filter>
					</activity>
				</application>
			</manifest>
		""".trimIndent()
		gradle.delete("src/main/AndroidManifest.xml")
		gradle.file(androidManifest, "src/main/AndroidManifest.xml")

		@Language("java")
		val activityCode = """
			package ${packageName};
			
			public class MainActivity extends android.app.Activity { }
		""".trimIndent()
		gradle.file(activityCode, "src/main/java/${packageName.replace('.', '/')}/MainActivity.java")

		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.android-app")
			}
			android.namespace = "${packageName}"
			afterEvaluate {
				// Don't always try to install the APK, as we may have no emulator,
				// but still assemble the APK, as the run task needs AndroidManifest.xml.
				tasks.installDebug.enabled = ${hasDevices}
			}
		""".trimIndent()

		if (hasDevices) {
			val result = gradle.run(script, "runDebug").build()

			result.assertSuccess(":packageDebug")
			result.assertSuccess(":installDebug")
			result.assertSuccess(":runDebug")
			// line is output to stderr, so no control over being on a new line
			result.assertNoOutputLine(""".*no devices/emulators found.*""".toRegex())
			result.assertNoOutputLine("""Error: Activity class .* does not exist\.""".toRegex())
		} else {
			val result = gradle.run(script, "runDebug").buildAndFail()

			result.assertSuccess(":packageDebug")
			result.assertSkipped(":installDebug")
			result.assertFailed(":runDebug")
			// The line is output to stderr, so no control over being on a new line after capturing Gradle output.
			result.assertHasOutputLine(""".*adb(\.exe)?: no devices/emulators found.*""".toRegex())
		}
	}
}
