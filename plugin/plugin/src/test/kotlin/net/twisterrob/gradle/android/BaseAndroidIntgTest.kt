package net.twisterrob.gradle.android

import net.twisterrob.gradle.test.GradleRunnerRule
import org.intellij.lang.annotations.Language
import org.junit.Rule
import org.junit.rules.TestName
import kotlin.test.BeforeTest

abstract class BaseAndroidIntgTest {

	@Rule @JvmField val gradle = GradleRunnerRule()
	@Rule @JvmField val testName = TestName()

	@BeforeTest fun setUp() {
		@Language("xml")
		val androidManifest = """
			<manifest package="${packageName}" />
		""".trimIndent()
		gradle.file(androidManifest, "src/main/AndroidManifest.xml")

		@Language("properties")
		val gradleProperties = """
			android.enableAapt2=false
		""".trimIndent()
		gradle.file(gradleProperties, "gradle.properties")

		if (testName.methodName.endsWith(" (release)")) {
			@Language("java")
			val apkContentForProguard = """
				package ${packageName};
				class AClassToSatisfyProguard {
					@android.webkit.JavascriptInterface public void f() {}
				}
			""".trimIndent()
			gradle.file(apkContentForProguard, "src/main/java/${packageFolder}/AClassToSatisfyProguard.java")
		}
	}
}
