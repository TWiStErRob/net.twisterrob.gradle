package net.twisterrob.gradle.java

import net.twisterrob.gradle.android.BaseAndroidIntgTest
import net.twisterrob.gradle.android.packageFolder
import net.twisterrob.gradle.android.packageName
import net.twisterrob.gradle.test.assertFailed
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.assertNoOutputLine
import net.twisterrob.gradle.test.assertSuccess
import org.intellij.lang.annotations.Language
import org.junit.Test

/**
 * @see JavaPlugin
 */
class JavaPluginIntgTest : BaseAndroidIntgTest() {

	@Test fun `test code supports Java 8`() {
		@Language("java")
		val java8InTest = """
			package ${packageName};
			import org.junit.Test;
			public class TestCode {
				@Test
				public void testJava8() {
					Runnable runnable = () -> { System.out.println("Lambda with " + BuildConfig.APPLICATION_ID); };
					runnable.run();
				}
			}
		""".trimIndent()
		gradle.file(java8InTest, "src/test/java/${packageFolder}/TestCode.java")
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
			dependencies {
				testImplementation "junit:junit:4.12"
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
