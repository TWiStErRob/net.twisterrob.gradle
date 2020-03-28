package net.twisterrob.gradle.java

import net.twisterrob.gradle.android.BaseAndroidIntgTest
import net.twisterrob.gradle.android.packageFolder
import net.twisterrob.gradle.android.packageName
import net.twisterrob.gradle.test.assertFailed
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.assertNoOutputLine
import net.twisterrob.gradle.test.assertNoSource
import net.twisterrob.gradle.test.assertSuccess
import org.intellij.lang.annotations.Language
import org.junit.Test

/**
 * @see BaseJavaPlugin
 * @see JavaPlugin
 * @see JavaLibPlugin
 */
class JavaPluginIntgTest : BaseAndroidIntgTest() {

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
		result.assertHasOutputLine(""".*Deprecation.java:5: warning: \[deprecation\] DeprecatedClass in ${packageName} has been deprecated""".toRegex())
	}

	@Test fun `test code supports Java 8`() {
		@Language("java")
		val java8InTest = """
			package ${packageName};
			import org.junit.Test;
			public class TestCode {
				@Test
				public void testJava8() {
					Runnable runnable = () -> System.out.println("Lambda with " + BuildConfig.APPLICATION_ID);
					runnable.run();
				}
			}
		""".trimIndent()
		gradle.file(java8InTest, "src/test/java/${packageFolder}/TestCode.java")
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
			dependencies {
				testImplementation "junit:junit:4.13"
			}
			tasks.withType(Test) {
				//noinspection UnnecessaryQualifiedReference
				testLogging.events = org.gradle.api.tasks.testing.logging.TestLogEvent.values().toList().toSet()
			}
		""".trimIndent()

		val result = gradle.run(script, "test").build()

		result.assertSuccess(":testDebugUnitTest")
		result.assertSuccess(":testReleaseUnitTest")
		result.assertHasOutputLine("""\s*Lambda with ${packageName}""".toRegex())
		result.assertHasOutputLine("""\s*Lambda with ${packageName}\.debug""".toRegex())
	}

	@Test fun `production code is strictly less than Java 8`() {
		@Language("java")
		val java7InProduction = """
			package ${packageName};
			public class ProductionCode {
				public void tryToUseLambda() {
					Runnable runnable = () -> { };
					runnable.run();
				}
			}
		""".trimIndent()
		gradle.file(java7InProduction, "src/main/java/${packageFolder}/ProductionCode.java")
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
		""".trimIndent()

		val result = gradle.run(script, "assembleDebug").buildAndFail()

		result.assertFailed(":compileDebugJavaWithJavac")
		result.assertHasOutputLine(""".*error: lambda expressions are not supported in -source (1\.)?7""".toRegex())
		result.assertHasOutputLine("\t\tRunnable runnable = () -> { };")
		result.assertHasOutputLine("  (use -source 8 or higher to enable lambda expressions)")
	}

	@Test fun `bootClasspath is correctly set`() {
		@Language("java")
		val emptyProductionClass = """
			package ${packageName};
			class EmptyProductionClass { }
		""".trimIndent()
		gradle.file(emptyProductionClass, "src/main/java/${packageFolder}/EmptyProductionClass.java")
		@Language("java")
		val emptyUnitTestClass = """
			package ${packageName};
			class EmptyUnitTestClass { }
		""".trimIndent()
		gradle.file(emptyUnitTestClass, "src/test/java/${packageFolder}/EmptyUnitTestClass.java")
		@Language("java")
		val emptyAndroidTestClass = """
			package ${packageName};
			class EmptyAndroidTestClass { }
		""".trimIndent()
		gradle.file(emptyAndroidTestClass, "src/androidTest/java/${packageFolder}/EmptyAndroidTestClass.java")
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
		""".trimIndent()

		val result = gradle.run(
			script,
			"compileDebugSources",
			"compileDebugUnitTestSources",
			"compileDebugAndroidTestSources"
		).build()

		result.assertSuccess(":compileDebugJavaWithJavac")
		result.assertSuccess(":compileDebugUnitTestJavaWithJavac")
		result.assertSuccess(":compileDebugAndroidTestJavaWithJavac")
		result.assertNoOutputLine(""".*bootstrap class path not set in conjunction with.*""".toRegex())
		result.assertNoOutputLine(""".*Java Compatibility: javac needs a bootclasspath.*""".toRegex())
	}
}
