package net.twisterrob.gradle.test

import net.twisterrob.gradle.BaseIntgTest
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Tests for writing a file to the Gradle project.
 *
 * These would be perfect for parameterization, but IDEA can't jump to the test case definition on failure,
 * nor it can debug a specific test case, so using a simple method call instead.
 *
 * @see GradleRunnerRule.addContents
 */
@ExtendWith(GradleRunnerRuleExtension::class)
class GradleRunnerRuleTest_file : BaseIntgTest() {

	override lateinit var gradle: GradleRunnerRule

	@Nested
	inner class `gradle scripts can be merged` {

		private fun mergeTest(
			@Language("gradle")
			script1: String,
			@Language("gradle")
			script2: String,
			@Language("gradle")
			mergeExpectation: String
		) {
			gradle.file(script1, GradleRunnerRule.TouchMode.MERGE_GRADLE, "build.gradle")
			gradle.file(script2, GradleRunnerRule.TouchMode.MERGE_GRADLE, "build.gradle")

			val merged = gradle.buildFile.readText()

			assertEquals(mergeExpectation, merged)
		}

		@Test fun `empty plugins blocks`() {
			mergeTest(
				script1 = """
					plugins {
					}
				""".trimIndent(),
				script2 = """
					plugins {
					}
				""".trimIndent(),
				mergeExpectation = """
					plugins {
					}
					plugins {
					}
				""".trimIndent()
			)
		}

		@Test fun `empty plugins and buildscript blocks in correct order`() {
			mergeTest(
				script1 = """
					buildscript {
					}
				""".trimIndent(),
				script2 = """
					plugins {
					}
				""".trimIndent(),
				mergeExpectation = """
					buildscript {
					}
					plugins {
					}
				""".trimIndent()
			)
		}

		@Test fun `empty plugins and buildscript blocks in wrong order`() {
			mergeTest(
				script1 = """
					plugins {
					}
				""".trimIndent(),
				script2 = """
					buildscript {
					}
				""".trimIndent(),
				mergeExpectation = """
					buildscript {
					}
					plugins {
					}
				""".trimIndent()
			)
		}

		@Test fun `simple plugins blocks`() {
			mergeTest(
				script1 = """
					plugins {
						id("org.gradle.java")
					}
				""".trimIndent(),
				script2 = """
					plugins {
						id("org.gradle.java-library")
					}
				""".trimIndent(),
				mergeExpectation = """
					plugins {
						id("org.gradle.java")
					}
					plugins {
						id("org.gradle.java-library")
					}
				""".trimIndent()
			)
		}

		@Test fun `append third to two plugins blocks`() {
			mergeTest(
				script1 = """
					plugins {
						id("org.gradle.java")
					}
					plugins {
						id("org.gradle.java-library")
					}
				""".trimIndent(),
				script2 = """
					plugins {
						id("org.jetbrains.kotlin.jvm")
					}
				""".trimIndent(),
				mergeExpectation = """
					plugins {
						id("org.gradle.java")
					}
					plugins {
						id("org.gradle.java-library")
					}
					plugins {
						id("org.jetbrains.kotlin.jvm")
					}
				""".trimIndent()
			)
		}

		@Test fun `plugins block inserted at the right place`() {
			mergeTest(
				script1 = """
					buildscript {
						dependencies {
							classpath("...")
						}
					}
					println("hello")
				""".trimIndent(),
				script2 = """
					plugins {
						id("org.jetbrains.kotlin.jvm")
					}
				""".trimIndent(),
				mergeExpectation = """
					buildscript {
						dependencies {
							classpath("...")
						}
					}
					plugins {
						id("org.jetbrains.kotlin.jvm")
					}
					println("hello")
				""".trimIndent()
			)
		}

		@Test fun `plugins block wrapped in buildscript and script content`() {
			mergeTest(
				script1 = """
					plugins {
						id("org.jetbrains.kotlin.jvm")
					}
				""".trimIndent(),
				script2 = """
					buildscript {
						dependencies {
							classpath("...")
						}
					}
					println("hello")
				""".trimIndent(),
				mergeExpectation = """
					buildscript {
						dependencies {
							classpath("...")
						}
					}
					plugins {
						id("org.jetbrains.kotlin.jvm")
					}
					println("hello")
				""".trimIndent()
			)
		}

		@Suppress("IncorrectPluginDslStructure") // Intentionally bad.
		@Test fun `messy full merge into correct script`() {
			mergeTest(
				script1 = """
					buildscript {
						dependencies {
							// Comment that has { } in it.
							classpath("...")
						}
					}
					plugins {
						id("org.jetbrains.kotlin.jvm")
					}
					println("hello")
				""".trimIndent(),
				script2 = """
					println("cruel")
					println("world")
					plugins {
						id("org.jetbrains.kotlin.kapt")
					}
					buildscript {
						dependencies {
							classpath("...")
						}
					}
					// More script.
					dependencies {
						implementation("...")
					}
				""".trimIndent(),
				mergeExpectation = """
					buildscript {
						dependencies {
							// Comment that has { } in it.
							classpath("...")
						}
					}
					buildscript {
						dependencies {
							classpath("...")
						}
					}
					plugins {
						id("org.jetbrains.kotlin.jvm")
					}
					plugins {
						id("org.jetbrains.kotlin.kapt")
					}
					println("hello")
					println("cruel")
					println("world")
					// More script.
					dependencies {
						implementation("...")
					}
				""".trimIndent()
			)
		}
	}
}
