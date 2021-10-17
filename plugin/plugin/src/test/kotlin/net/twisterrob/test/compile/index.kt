package net.twisterrob.test.compile

import net.twisterrob.gradle.android.packageFolder
import net.twisterrob.gradle.android.packageName
import net.twisterrob.gradle.test.GradleRunnerRule
import org.intellij.lang.annotations.Language

fun GradleRunnerRule.generateKotlinCompilationCheck(rootFolder: String = "") {
	@Language("kotlin")
	val kotlinClass = """
				package ${packageName}
				class KotlinCompilationCheck
			""".trimIndent()
	this.file(kotlinClass, "${rootFolder}/src/main/kotlin/${packageFolder}/KotlinCompilationCheck.kt")
}

fun GradleRunnerRule.generateKotlinCompilationCheckTest(rootFolder: String = "") {
	@Language("kotlin")
	val kotlinTestClass = """
				package ${packageName}
				import kotlin.test.assertNotNull
				import kotlin.test.Test
				class KotlinCompilationCheckTest {
					@Test fun test() {
						assertNotNull(KotlinCompilationCheck())
					}
				}
			""".trimIndent()
	this.file(kotlinTestClass, "${rootFolder}/src/test/kotlin/${packageFolder}/KotlinCompilationCheckTest.kt")
}
