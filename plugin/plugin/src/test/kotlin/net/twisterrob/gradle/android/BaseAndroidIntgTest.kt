package net.twisterrob.gradle.android

import net.twisterrob.gradle.BaseIntgTest
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInfo

abstract class BaseAndroidIntgTest : BaseIntgTest() {

	@BeforeEach fun setUp(testInfo : TestInfo) {
		@Language("xml")
		val androidManifest = """
			<manifest package="${packageName}" />
		""".trimIndent()
		gradle.file(androidManifest, "src/main/AndroidManifest.xml")

		@Language("properties")
		val versionProperties = """
			# Since AGP 3.3 versionCode must be > 0
			build=1
		""".trimIndent()
		gradle.file(versionProperties, "version.properties")

		if (testInfo.testMethod.get().name.endsWith(" (release)")) {
			createFileToMakeSureProguardPasses()
		}
	}

	protected fun createFileToMakeSureProguardPasses() {
		@Language("java")
		val apkContentForProguard = """
			package ${packageName};
			class AClassToSatisfyProguard {
				@android.webkit.JavascriptInterface public void f() {}
			}
		""".trimIndent()
		gradle.file(apkContentForProguard, "src/main/java/${packageFolder}/AClassToSatisfyProguard.java")

		@Language("properties")
		val props = """
			android.enableR8=false

		""".trimIndent() // extra empty line important, as others may append to this file
		gradle.file(props, "gradle.properties")
	}
}
