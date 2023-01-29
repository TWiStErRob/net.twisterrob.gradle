package net.twisterrob.gradle.java

import net.twisterrob.gradle.android.BaseAndroidIntgTest
import net.twisterrob.gradle.android.packageFolder
import net.twisterrob.gradle.android.packageName
import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.assertNoOutputLine
import net.twisterrob.gradle.test.assertNoSource
import net.twisterrob.gradle.test.assertSuccess
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * @see BaseJavaPlugin
 * @see JavaPlugin
 * @see JavaLibPlugin
 */
@ExtendWith(GradleRunnerRuleExtension::class)
class JavaPluginIntgTest : BaseAndroidIntgTest() {

	override lateinit var gradle: GradleRunnerRule

	@BeforeEach fun setMemory() {
		gradle.file("org.gradle.jvmargs=-Xmx512M -XX:MaxMetaspaceSize=384M\n", "gradle.properties")
	}

	@Test fun `java plugin can be applied standalone`() {
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.java'
			println("Java: " + plugins.hasPlugin("java"))
			println("Java Library: " + plugins.hasPlugin("java-library"))
		""".trimIndent()

		val result = gradle.run(script, "build").build()

		result.assertNoSource(":compileJava")
		result.assertSuccess(":jar")
		result.assertHasOutputLine("""Java: true""".toRegex())
		result.assertHasOutputLine("""Java Library: false""".toRegex())
	}

	@Test fun `java-library plugin can be applied standalone`() {
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.java-library'
			println("Java: " + plugins.hasPlugin("java"))
			println("Java Library: " + plugins.hasPlugin("java-library"))
		""".trimIndent()

		val result = gradle.run(script, "build").build()

		result.assertNoSource(":compileJava")
		result.assertSuccess(":jar")
		result.assertHasOutputLine("""Java: true""".toRegex())
		result.assertHasOutputLine("""Java Library: true""".toRegex())
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
			apply plugin: 'net.twisterrob.java'
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
			apply plugin: 'net.twisterrob.java'
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
			apply plugin: 'net.twisterrob.java'
		""".trimIndent()

		val result = gradle.run(script, "build").build()

		result.assertSuccess(":compileJava")
		result.assertNoOutputLine("""Note: .*Deprecation.java uses or overrides a deprecated API.""".toRegex())
		result.assertNoOutputLine("""Note: Recompile with -Xlint:deprecation for details.""".toRegex())
		val pack = Regex.escape(packageName)
		result.assertHasOutputLine(""".*Deprecation.java:5: warning: \[deprecation\] (${pack}\.)?DeprecatedClass in ${pack} has been deprecated""".toRegex())
	}
}
