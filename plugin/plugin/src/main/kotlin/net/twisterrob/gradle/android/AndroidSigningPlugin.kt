package net.twisterrob.gradle.android

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.dsl.SigningConfig
import net.twisterrob.gradle.common.BasePluginForKotlin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.get

class AndroidSigningPlugin : BasePluginForKotlin() {
	companion object {
		private const val SIGNING_CONFIG_NAME = "twisterrob"
	}

	override fun apply(target: Project) {
		super.apply(target)

		val keyStoreFile = project.file(optionalProp("RELEASE_STORE_FILE") ?: "twisterrob.jks")
		if (keyStoreFile.isFile && keyStoreFile.exists() && keyStoreFile.canRead()) {
			LOG.info("Attaching release.signingConfig.{} using '{}'", SIGNING_CONFIG_NAME, keyStoreFile)
			val android = project.extensions["android"] as BaseExtension
			val sign = SigningConfig(SIGNING_CONFIG_NAME).apply {
				storeFile = keyStoreFile
				storePassword = mandatoryProp("RELEASE_STORE_PASSWORD")
				keyAlias = optionalProp("RELEASE_KEY_ALIAS") ?: "net.twisterrob"
				keyPassword = mandatoryProp("RELEASE_KEY_PASSWORD")
			}
			//android.signingConfigs.add(sign)
			android.buildTypes["release"].signingConfig = sign
		} else if (project.hasProperty("RELEASE_STORE_FILE")) {
			LOG.error("Keystore file '{}' is not valid.", keyStoreFile.absolutePath)
		}
	}

	private fun optionalProp(name: String): String? = project.properties[name] as String?
	private fun mandatoryProp(name: String): String = optionalProp(name)!!
}
