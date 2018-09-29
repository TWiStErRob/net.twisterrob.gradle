package net.twisterrob.gradle.android

import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.assertSuccess
import net.twisterrob.gradle.test.root
import net.twisterrob.test.process.runCommand
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.emptyString
import org.hamcrest.junit.MatcherAssert.assertThat
import org.intellij.lang.annotations.Language
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/**
 * @see AndroidSigningPlugin
 */
class AndroidSigningPluginIntgTest : BaseAndroidIntgTest() {

	@Rule @JvmField val temp = TemporaryFolder()

	@Test fun `logs error when keystore not valid (release)`() {
		@Language("properties")
		val props = """
			# suppress inspection "UnusedProperty"
			RELEASE_STORE_FILE=non-existent.file
		""".trimIndent()
		gradle.root.resolve("gradle.properties").appendText(props)

		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
		""".trimIndent()

		val result = gradle.run(script, "assembleRelease").build()

		result.assertSuccess(":assembleRelease")
		result.assertHasOutputLine("""Keystore file \(from RELEASE_STORE_FILE\) '.*non-existent.file.*' is not valid\.""".toRegex())
		verifyWithJarSigner(gradle.root.apk("release").absolutePath).also {
			assertThat(it, containsString("jar is unsigned."))
		}
	}

	@Test fun `applies signing config from properties (release)`() {
		val generationParams = mapOf(
			"-alias" to "gradle.plugin.test",
			"-keyalg" to "RSA",
			"-keystore" to "gradle.plugin.test.jks",
			"-dname" to "CN=JUnit Test, O=net.twisterrob, L=Gradle",
			"-storepass" to "testStorePassword",
			"-keypass" to "testKeyPassword"
		)
		listOf(
			resolveFromJDK("keytool").absolutePath,
			"-genkey",
			*generationParams.flatMap { it.toPair().toList() }.toTypedArray()
		).runCommand(temp.root).also {
			assertThat(it, emptyString())
		}

		@Language("properties")
		val props = """
			# suppress inspection "UnusedProperty" for whole file
			RELEASE_STORE_FILE=${temp.root.resolve(generationParams["-keystore"]!!).absolutePath.replace("\\", "\\\\")}
			RELEASE_STORE_PASSWORD=${generationParams["-storepass"]}
			RELEASE_KEY_ALIAS=${generationParams["-alias"]}
			RELEASE_KEY_PASSWORD=${generationParams["-keypass"]}
		""".trimIndent()
		gradle.root.resolve("gradle.properties").appendText(props)

		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
		""".trimIndent()

		val result = gradle.run(script, "assembleRelease").build()

		result.assertSuccess(":assembleRelease")

		verifyWithApkSigner(gradle.root.apk("release").absolutePath).also {
			assertThat(it, emptyString())
		}
		verifyWithJarSigner(gradle.root.apk("release").absolutePath).also {
			assertThat(it, allOf(containsString("jar verified."), containsString(generationParams["-dname"])))
		}
	}

	private fun verifyWithApkSigner(apk: String) = listOf(
		resolveFromAndroidSDK("apksigner").absolutePath,
		"verify",
		apk
	).runCommand()

	private fun verifyWithJarSigner(apk: String) = listOf(
		resolveFromJDK("jarsigner").absolutePath,
		"-verify",
		"-verbose",
		apk
	).runCommand()
}
