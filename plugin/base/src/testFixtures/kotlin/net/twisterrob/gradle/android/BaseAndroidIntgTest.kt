package net.twisterrob.gradle.android

import net.twisterrob.gradle.BaseIntgTest
import net.twisterrob.gradle.test.GradleBuildTestResources
import net.twisterrob.gradle.test.GradleBuildTestResources.basedOn
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInfo

abstract class BaseAndroidIntgTest : BaseIntgTest() {

	@BeforeEach fun setUp(testInfo: TestInfo) {
		gradle.basedOn(GradleBuildTestResources.android)

		if (testInfo.testMethod.get().name.endsWith(" (release)")) {
			createFileToMakeProguardPass()
		}
	}

	protected fun createFileToMakeProguardPass() {
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
