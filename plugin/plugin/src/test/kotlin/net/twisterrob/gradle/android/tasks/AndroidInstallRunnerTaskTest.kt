package net.twisterrob.gradle.android.tasks

import net.twisterrob.gradle.android.packageName
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlin.test.assertEquals

class AndroidInstallRunnerTaskTest {

	@Rule @JvmField val temp = TemporaryFolder()

	@Test fun `single main activity with extra category`() {
		val file = temp.newFile()
		file.writeText(
			"""
			<?xml version="1.0" encoding="utf-8"?>
			<manifest xmlns:android="http://schemas.android.com/apk/res/android" package="${packageName}">
				<!-- Extra permission for more flexibility in test -->
				<uses-permission android:name="android.permission.INTERNET" />
				<application>
					<activity android:name=".activity.MainActivity">
						<intent-filter>
							<action android:name="android.intent.action.MAIN" />
							<category android:name="android.intent.category.LAUNCHER" />
							<!-- Extra permission for more flexibility in test -->
							<category android:name="android.intent.category.DEFAULT" />
						</intent-filter>
					</activity>
				</application>
			</manifest>
			""".trimIndent()
		)

		val mainActivity = AndroidInstallRunnerTask.getMainActivity(file)

		assertEquals(".activity.MainActivity", mainActivity)
	}
}
