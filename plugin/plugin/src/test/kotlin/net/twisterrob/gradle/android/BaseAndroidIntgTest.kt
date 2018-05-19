package net.twisterrob.gradle.android

import net.twisterrob.gradle.test.GradleRunnerRule
import org.intellij.lang.annotations.Language
import org.junit.Rule
import org.junit.rules.TestName
import kotlin.test.BeforeTest

open class BaseAndroidIntgTest {

	@Rule @JvmField val gradle = GradleRunnerRule()
	@Rule @JvmField val testName = TestName()

	@BeforeTest fun setUp() {
		@Language("xml")
		val manifest = """
			<manifest package="${packageName}" />
		""".trimIndent()
		gradle.file(manifest, "src/main/AndroidManifest.xml")

		@Language("properties")
		val gradle_properties = """
			android.enableAapt2=false
		""".trimIndent()
		gradle.file(gradle_properties, "gradle.properties")

		if (testName.methodName.endsWith(" (release)")) {
			@Language("java")
			val custom_view = """
				package ${packageName};
				class AClassToSatisfyProguard {
					@android.webkit.JavascriptInterface public void f() {}
				}
			""".trimIndent()
			val packageFolder = packageName.replace('.', '/')
			gradle.file(custom_view, "src/main/java/${packageFolder}/AClassToSatisfyProguard.java")
		}
	}
}
