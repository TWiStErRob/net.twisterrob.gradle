package net.twisterrob.gradle.android.tasks

import net.twisterrob.gradle.android.packageName
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AndroidInstallRunnerTaskTest {

	@Test fun `one match in the middle`() {
		@Language("xml")
		val androidManifest =
			"""<?xml version="1.0" encoding="utf-8"?>
			<manifest xmlns:android="http://schemas.android.com/apk/res/android" package="${packageName}">
				<!-- Extra permission for more flexibility in test -->
				<uses-permission android:name="android.permission.INTERNET" />
				<application>
					<activity android:name=".missing.filter" />
					<activity android:name=".missing.action">
						<intent-filter>
							<category android:name="android.intent.category.LAUNCHER" />
						</intent-filter>
					</activity>
					<activity android:name=".missing.category">
						<intent-filter>
							<action android:name="android.intent.action.MAIN" />
						</intent-filter>
					</activity>
					<activity android:name=".activity.MainActivity">
						<intent-filter>
							<action android:name="android.intent.action.MAIN" />
							<category android:name="android.intent.category.LAUNCHER" />
							<!-- Extra category for more flexibility in test -->
							<category android:name="android.intent.category.DEFAULT" />
						</intent-filter>
					</activity>
					<activity android:name=".missing.contents">
						<intent-filter>
						</intent-filter>
					</activity>
					<activity android:name=".multiple.filters">
						<intent-filter>
							<action android:name="android.intent.action.MAIN" />
						</intent-filter>
						<intent-filter />
						<intent-filter>
							<category android:name="android.intent.category.LAUNCHER" />
						</intent-filter>
					</activity>
				</application>
			</manifest>
			""".trimIndent()

		val mainActivity = AndroidInstallRunnerTask.getMainActivity(androidManifest.byteInputStream())

		assertEquals(".activity.MainActivity", mainActivity)
	}
}
