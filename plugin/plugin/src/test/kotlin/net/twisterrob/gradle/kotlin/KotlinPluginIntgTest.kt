package net.twisterrob.gradle.kotlin

import net.twisterrob.gradle.android.BaseAndroidIntgTest
import net.twisterrob.gradle.android.assertSuccess
import net.twisterrob.gradle.android.packageFolder
import net.twisterrob.gradle.android.packageName
import org.intellij.lang.annotations.Language
import org.junit.Test

/**
 * @see KotlinPlugin
 */
class KotlinPluginIntgTest : BaseAndroidIntgTest() {

	@Test fun `can compile Kotlin`() {
		generateKotlinCompilationCheck()

		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.kotlin'
		""".trimIndent()

		val result = gradle.run(script, "jar").build()

		result.assertSuccess(":compileKotlin")
	}

	@Test fun `can test Kotlin with TestNG`() {
		generateKotlinCompilationCheck()

		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.kotlin'
			dependencies {
				testImplementation "org.testng:testng:6.10"
			}
		""".trimIndent()

		@Language("kotlin")
		val kotlinTestClass = """
			package ${packageName}
			import kotlin.test.assertNotNull
			import org.testng.annotations.Test
			class KotlinCompilationCheckTest {
				@Test fun test() {
					assertNotNull(KotlinCompilationCheck())
				}
			}
		""".trimIndent()
		gradle.file(kotlinTestClass, "src/test/kotlin/${packageFolder}/KotlinCompilationCheckTest.kt")

		val result = gradle.run(script, "test").build()

		result.assertSuccess(":compileKotlin")
		result.assertSuccess(":compileTestKotlin")
	}

	@Test fun `can test kotlin with JUnit (dependency is auto-added)`() {
		generateKotlinCompilationCheck()

		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.kotlin'
			dependencies {
				testImplementation "junit:junit:4.12"
			}
		""".trimIndent()

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
		gradle.file(kotlinTestClass, "src/test/kotlin/${packageFolder}/KotlinCompilationCheckTest.kt")

		val result = gradle.run(script, "test").build()

		result.assertSuccess(":compileKotlin")
		result.assertSuccess(":compileTestKotlin")
	}

	private fun generateKotlinCompilationCheck() {
		@Language("kotlin")
		val kotlinClass = """
				package ${packageName}
				class KotlinCompilationCheck
			""".trimIndent()
		gradle.file(kotlinClass, "src/main/kotlin/${packageFolder}/KotlinCompilationCheck.kt")
	}
}
