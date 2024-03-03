package net.twisterrob.gradle.android

import net.twisterrob.gradle.common.AGPVersions
import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.assertSuccess
import net.twisterrob.gradle.test.root
import net.twisterrob.test.process.runCommand
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.emptyString
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * @see AndroidSigningPlugin
 */
@ExtendWith(GradleRunnerRuleExtension::class)
class AndroidSigningPluginIntgTest : BaseAndroidIntgTest() {

	override lateinit var gradle: GradleRunnerRule

	@Test fun `logs error when keystore not valid (release)`() {
		@Language("properties")
		val props = """
			# suppress inspection "UnusedProperty"
			RELEASE_STORE_FILE=non-existent.file
		""".trimIndent()
		gradle.root.resolve("gradle.properties").appendText(props)

		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.android-app")
			}
		""".trimIndent()

		val result = gradle.run(script, "assembleRelease").build()

		result.assertSuccess(":assembleRelease")
		result.assertHasOutputLine(
			"""
				Keystore file \(from RELEASE_STORE_FILE\) '.*non-existent.file.*' is not valid\.
			""".trimIndent().toRegex()
		)
		verifyWithJarSigner(gradle.root.apk("release").absolutePath).also {
			assertThat(it, containsString("jar is unsigned."))
		}
	}

	@Suppress("detekt.LongMethod")
	@Test fun `applies signing config from properties (release)`(@TempDir temp: File) {
		val generationParams = mapOf(
			"-alias" to "gradle.plugin.test",
			"-keyalg" to "RSA",
			"-keystore" to "gradle.plugin.test.jks",
			"-storetype" to "JKS",
			"-dname" to "CN=JUnit Test, O=net.twisterrob, L=Gradle",
			"-storepass" to "testStorePassword",
			"-keypass" to "testKeyPassword"
		)
		listOf(
			resolveFromJDK("keytool").absolutePath,
			"-genkey",
			*generationParams.flatMap { it.toPair().toList() }.toTypedArray()
		).runCommand(temp).also {
			assertThat(it, emptyString())
		}

		@Language("properties")
		val props = """
			# suppress inspection "UnusedProperty" for whole file
			RELEASE_STORE_FILE=${temp.resolve(generationParams.getValue("-keystore")).absolutePath.replace("\\", "\\\\")}
			RELEASE_STORE_PASSWORD=${generationParams.getValue("-storepass")}
			RELEASE_KEY_ALIAS=${generationParams.getValue("-alias")}
			RELEASE_KEY_PASSWORD=${generationParams.getValue("-keypass")}
		""".trimIndent()
		gradle.root.resolve("gradle.properties").appendText(props)

		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.android-app")
			}
			// Increase minimum version to avoid ignoring the signature:
			// > jarsigner WARNING: The jar will be treated as unsigned,
			// > because it is signed with a weak algorithm that is now disabled by the security property
			// This happens on GitHub Actions ubuntu-2204 runners because java.security file contains:
			// jdk.jar.disabledAlgorithms=SHA1 denyAfter 2019-01-01
			android.defaultConfig.minSdkVersion = 21
		""".trimIndent()

		val result = gradle.run(script, "assembleRelease").build()

		result.assertSuccess(":assembleRelease")

		verifyWithApkSigner(gradle.root.apk("release").absolutePath).also { apkSignerOutput ->
			val expectedWarnings = listOfNotNull(
				if (AGPVersions.v70x <= AGPVersions.UNDER_TEST) {
					// REPORT this should be empty, AGP 4.2.0 introduced this file.
					unprotectedJarEntry("com/android/build/gradle/app-metadata.properties")
				} else {
					null
				},
				if (AGPVersions.v83x <= AGPVersions.UNDER_TEST) {
					// REPORT this should be empty, AGP 8.3.0 introduced this file.
					unprotectedJarEntry("version-control-info.textproto")
				} else {
					null
				},
			)
			assertEquals(
				expectedWarnings.joinToString(separator = ""),
				apkSignerOutput
			)
		}
		verifyWithJarSigner(gradle.root.apk("release").absolutePath).also {
			assertThat(it, allOf(containsString("jar verified."), containsString(generationParams["-dname"])))
		}
	}

	private fun verifyWithApkSigner(apk: String): String =
		listOf(
			// apksigner.bat doesn't work with Java 11, even though everything is set up correctly:
			// it says "No suitable Java found.", "you can define the JAVA_HOME environment variable"
			// so as an alternative launch the jar file directly
			resolveFromJDK("java").absolutePath,
			"-jar",
			resolveFromAndroidSDK("apksigner").parentFile.resolve("lib/apksigner.jar").absolutePath,
			"verify",
			apk
		).runCommand()

	private fun verifyWithJarSigner(apk: String): String =
		listOf(
			resolveFromJDK("jarsigner").absolutePath,
			"-verify",

			// Turn this on to see more output in case of failure.
			"-verbose", // Print details of files.

			// Can't turn this on, because of warnings that cannot be fixed reasonably.
			//"-strict", // Make warnings on stderr into errors, with non-0 exit code.

			// Don't include this in repo, because it hangs the test on local execution.
			//"-J-Djava.security.debug=jar", // Print details of JAR verification.

			apk
		).runCommand()

	private fun unprotectedJarEntry(path: String): String =
		"WARNING: " +
				"META-INF/${path} not protected by signature. " +
				"Unauthorized modifications to this JAR entry will not be detected. " +
				"Delete or move the entry outside of META-INF/." +
				System.lineSeparator()
}
