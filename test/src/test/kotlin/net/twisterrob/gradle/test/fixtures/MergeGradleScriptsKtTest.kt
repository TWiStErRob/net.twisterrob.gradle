package net.twisterrob.gradle.test.fixtures

import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Tests for writing a file to the Gradle project.
 *
 * These would be perfect for parameterization, but IDEA can't jump to the test case definition on failure,
 * nor it can debug a specific test case, so using a simple method call instead.
 *
 * @see mergeGradleScripts
 * @see ContentMergeMode.MERGE_GRADLE
 */
class MergeGradleScriptsKtTest {

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
			val merged = mergeGradleScripts(script1, script2)

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

		@Test fun `empty plugins blocks with comments`() {
			mergeTest(
				script1 = """
					// before1
					plugins {
					}
					// after1
				""".trimIndent(),
				script2 = """
					// before2
					plugins {
					}
					// after2
				""".trimIndent(),
				mergeExpectation = """
					plugins {
					}
					plugins {
					}
					// before1
					// after1
					// before2
					// after2
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

		@Test fun `empty plugins block with another block after it`() {
			mergeTest(
				script1 = """
					plugins {
					}
					foo1 {
						// foo1
					}
				""".trimIndent(),
				script2 = """
					plugins {
					}
					foo2 {
						// foo2
					}
				""".trimIndent(),
				mergeExpectation = """
					plugins {
					}
					plugins {
					}
					foo1 {
						// foo1
					}
					foo2 {
						// foo2
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
							classpath("foo:bar:baz")
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
							classpath("foo:bar:baz")
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
							classpath("foo:bar:baz")
						}
					}
					println("hello")
				""".trimIndent(),
				mergeExpectation = """
					buildscript {
						dependencies {
							classpath("foo:bar:baz")
						}
					}
					plugins {
						id("org.jetbrains.kotlin.jvm")
					}
					println("hello")
				""".trimIndent()
			)
		}

		@Test fun `plugins block with content is merged in the right order`() {
			mergeTest(
				script1 = """
					buildscript {
					}
					// after buildscript
				""".trimIndent(),
				script2 = """
					plugins {
					}
					println("after plugins")
				""".trimIndent(),
				mergeExpectation = """
					buildscript {
					}
					plugins {
					}
					// after buildscript
					println("after plugins")
				""".trimIndent()
			)
		}

		@Test fun `plugins block with content is merged in the right order - Windows`() {
			mergeTest(
				script1 = """
					buildscript {
					}
					// after buildscript
				""".trimIndent().replace("\n", "\r\n"),
				script2 = """
					plugins {
					}
					println("after plugins")
				""".trimIndent().replace("\n", "\r\n"),
				mergeExpectation = """
					buildscript {
					}
					plugins {
					}
					// after buildscript
					println("after plugins")
				""".trimIndent()
			)
		}

		@Test fun `imports and code`() {
			mergeTest(
				script1 = """
					import org.gradle.api.Project
					// code1
				""".trimIndent(),
				script2 = """
					import org.gradle.api.Task
					// code2
				""".trimIndent(),
				mergeExpectation = """
					import org.gradle.api.Project
					import org.gradle.api.Task
					// code1
					// code2
				""".trimIndent()
			)
		}

		@Test fun `imports and code with spacing`() {
			mergeTest(
				script1 = """
					import org.gradle.api.Project
					
					// code1
				""".trimIndent(),
				script2 = """
					import org.gradle.api.Task
					
					// code2
				""".trimIndent(),
				mergeExpectation = """
					import org.gradle.api.Project
					import org.gradle.api.Task
					
					// code1
					
					// code2
				""".trimIndent()
			)
		}

		@Test fun `multi-imports and code`() {
			mergeTest(
				script1 = """
					import org.gradle.api.Project
					import java.io.File
					// code1
				""".trimIndent(),
				script2 = """
					import org.gradle.api.Task
					import java.io.InputStream
					// code2
				""".trimIndent(),
				mergeExpectation = """
					import org.gradle.api.Project
					import java.io.File
					import org.gradle.api.Task
					import java.io.InputStream
					// code1
					// code2
				""".trimIndent()
			)
		}

		@Test fun `plugins, buildscripts and imports`() {
			mergeTest(
				script1 = """
					import org.gradle.api.Project
					plugins {
						id("some.plugin")
					}
					// code
				""".trimIndent(),
				script2 = """
					import static org.gradle.api.plugins.JavaPlugin.*
					buildscript {
					}
				""".trimIndent(),
				mergeExpectation = """
					import org.gradle.api.Project
					import static org.gradle.api.plugins.JavaPlugin.*
					buildscript {
					}
					plugins {
						id("some.plugin")
					}
					// code
				""".trimIndent()
			)
		}

		@Suppress("GroovyAssignabilityCheck") // Intentionally wrong.
		@Test fun `leaves nested plugins blocks alone`() {
			mergeTest(
				script1 = """
					allprojects {
						plugins {
							id("net.twisterrob.gradle.plugin.checkstyle")
						}
					}
				""".trimIndent(),
				script2 = """
					subprojects {
						plugins {
							id("net.twisterrob.gradle.plugin.pmd")
						}
					}
				""".trimIndent(),
				mergeExpectation = """
					allprojects {
						plugins {
							id("net.twisterrob.gradle.plugin.checkstyle")
						}
					}
					subprojects {
						plugins {
							id("net.twisterrob.gradle.plugin.pmd")
						}
					}
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
							classpath("foo:bar:baz")
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
							classpath("foo:bar:baz")
						}
					}
					// More script.
					dependencies {
						implementation("foo:bar:baz")
					}
				""".trimIndent(),
				mergeExpectation = """
					buildscript {
						dependencies {
							// Comment that has { } in it.
							classpath("foo:bar:baz")
						}
					}
					buildscript {
						dependencies {
							classpath("foo:bar:baz")
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
						implementation("foo:bar:baz")
					}
				""".trimIndent()
			)
		}
	}
}
