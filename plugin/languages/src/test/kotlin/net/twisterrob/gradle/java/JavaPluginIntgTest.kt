package net.twisterrob.gradle.java

import net.twisterrob.gradle.BaseIntgTest
import net.twisterrob.gradle.android.packageFolder
import net.twisterrob.gradle.android.packageName
import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.assertNoOutputLine
import net.twisterrob.gradle.test.assertNoSource
import net.twisterrob.gradle.test.assertSuccess
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * @see BaseJavaPlugin
 * @see JavaPlugin
 * @see JavaLibPlugin
 */
@ExtendWith(GradleRunnerRuleExtension::class)
class JavaPluginIntgTest : BaseIntgTest() {

	override lateinit var gradle: GradleRunnerRule

	@Test fun `java plugin can be applied standalone`() {
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.java")
			}
			println("Java: " + plugins.hasPlugin("java"))
			println("Java Library: " + plugins.hasPlugin("java-library"))
		""".trimIndent()

		val result = gradle.run(script, "build").build()

		result.assertNoSource(":compileJava")
		result.assertSuccess(":jar")
		result.assertHasOutputLine("""Java: true""".toRegex())
		result.assertHasOutputLine("""Java Library: false""".toRegex())
	}

	@Test fun `applying java by the old name is deprecated`() {
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.java")
			}
		""".trimIndent()

		val result = gradle.run(script).buildAndFail()

		result.assertHasOutputLine(
			Regex(
				"""org\.gradle\.api\.GradleException: """ +
						"""Deprecated Gradle features were used in this build, making it incompatible with Gradle \d.0"""
			)
		)
		result.assertHasOutputLine(
			Regex(
				"""The net\.twisterrob\.java plugin has been deprecated\. """
						+ """This is scheduled to be removed in Gradle \d\.0\. """
						+ """Please use the net\.twisterrob\.gradle\.plugin\.java plugin instead."""
			)
		)
	}

	@Test fun `java-library plugin can be applied standalone`() {
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.java-library")
			}
			println("Java: " + plugins.hasPlugin("java"))
			println("Java Library: " + plugins.hasPlugin("java-library"))
		""".trimIndent()

		val result = gradle.run(script, "build").build()

		result.assertNoSource(":compileJava")
		result.assertSuccess(":jar")
		result.assertHasOutputLine("""Java: true""".toRegex())
		result.assertHasOutputLine("""Java Library: true""".toRegex())
	}

	@Test fun `applying java-library by the old name is deprecated`() {
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.java-library")
			}
		""".trimIndent()

		val result = gradle.run(script).buildAndFail()

		result.assertHasOutputLine(
			Regex(
				"""org\.gradle\.api\.GradleException: """ +
						"""Deprecated Gradle features were used in this build, making it incompatible with Gradle \d.0"""
			)
		)
		result.assertHasOutputLine(
			Regex(
				"""The net\.twisterrob\.java-library plugin has been deprecated\. """
						+ """This is scheduled to be removed in Gradle \d\.0\. """
						+ """Please use the net\.twisterrob\.gradle\.plugin\.java-library plugin instead."""
			)
		)
	}

	@Test fun `default-enabled warnings can be turned off`() {
		@Language("java")
		val uncheckedWarning = """
			package ${packageName};
			import java.util.ArrayList;
			import java.util.List;
			public class Unchecked {
				@SuppressWarnings("UnnecessaryLocalVariable")
				public void unchecked() {
					List<String> strings = new ArrayList<>();
					List<?> list = strings;
					//noinspection unchecked suppressed in code, but javac still picks it up
					List<Number> numbers = (List<Number>) list;
				}
			}
		""".trimIndent()
		gradle.file(uncheckedWarning, "src/main/java/${packageFolder}/Unchecked.java")
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.java")
			}
			tasks.named("compileJava").configure {
				it.options.compilerArgs += [
					// Disable default-enabled warnings (restore javac default state)
					"-Xlint:-unchecked"
				]
			}
		""".trimIndent()

		val result = gradle.run(script, "build").build()

		result.assertSuccess(":compileJava")
		result.assertHasOutputLine("""Note: .*Unchecked.java uses unchecked or unsafe operations.""".toRegex())
		result.assertHasOutputLine("""Note: Recompile with -Xlint:unchecked for details.""".toRegex())
	}

	@Test fun `unchecked warnings show up`() {
		@Language("java")
		val uncheckedWarning = """
			package ${packageName};
			import java.util.ArrayList;
			import java.util.List;
			public class Unchecked {
				@SuppressWarnings("UnnecessaryLocalVariable")
				public void unchecked() {
					List<String> strings = new ArrayList<>();
					List<?> list = strings;
					//noinspection unchecked suppressed in code, but javac still picks it up
					List<Number> numbers = (List<Number>) list;
				}
			}
		""".trimIndent()
		gradle.file(uncheckedWarning, "src/main/java/${packageFolder}/Unchecked.java")
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.java")
			}
		""".trimIndent()

		val result = gradle.run(script, "build").build()

		result.assertSuccess(":compileJava")
		result.assertNoOutputLine("""Note: .*Unchecked.java uses unchecked or unsafe operations.""".toRegex())
		result.assertNoOutputLine("""Note: Recompile with -Xlint:unchecked for details.""".toRegex())
		result.assertHasOutputLine(""".*Unchecked.java:10: warning: \[unchecked\] unchecked cast""".toRegex())
	}

	@Test fun `deprecation warnings show up`() {
		@Language("java")
		val uncheckedWarning = """
			package ${packageName};
			public class Deprecation {
				public void deprecation() {
					//noinspection deprecation suppressed in code, but javac still picks it up
					new DeprecatedClass();
				}
			}
			
			@SuppressWarnings("DeprecatedIsStillUsed")
			@Deprecated
			class DeprecatedClass {
			}
		""".trimIndent()
		gradle.file(uncheckedWarning, "src/main/java/${packageFolder}/Deprecation.java")
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.java")
			}
		""".trimIndent()

		val result = gradle.run(script, "build").build()

		result.assertSuccess(":compileJava")
		result.assertNoOutputLine("""Note: .*Deprecation.java uses or overrides a deprecated API.""".toRegex())
		result.assertNoOutputLine("""Note: Recompile with -Xlint:deprecation for details.""".toRegex())
		val pack = Regex.escape(packageName)
		result.assertHasOutputLine(
			"""
				.*Deprecation.java:5: warning: \[deprecation\] (${pack}\.)?DeprecatedClass in ${pack} has been deprecated
			""".trimIndent().toRegex()
		)
	}
}
