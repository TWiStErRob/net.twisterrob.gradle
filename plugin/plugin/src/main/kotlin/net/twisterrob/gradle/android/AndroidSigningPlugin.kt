package net.twisterrob.gradle.android

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.dsl.SigningConfig
import net.twisterrob.gradle.base.BasePlugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.get

class AndroidSigningPlugin : BasePlugin() {

	companion object {
		private const val SIGNING_CONFIG_NAME = "twisterrob"

		private const val DEFAULT_STORE_FILE = "twisterrob.jks"
		private const val STORE_FILE = "RELEASE_STORE_FILE"
		private const val STORE_PASSWORD = "RELEASE_STORE_PASSWORD"

		private const val DEFAULT_KEY_ALIAS = "net.twisterrob"
		private const val KEY_ALIAS = "RELEASE_KEY_ALIAS"
		private const val KEY_PASSWORD = "RELEASE_KEY_PASSWORD"
	}

	override fun apply(target: Project) {
		super.apply(target)

		val keyStoreFile = project.file(optionalProp(STORE_FILE) ?: DEFAULT_STORE_FILE)
		if (keyStoreFile.isFile && keyStoreFile.exists() && keyStoreFile.canRead()) {
			LOG.info("Attaching release.signingConfig.{} using '{}'", SIGNING_CONFIG_NAME, keyStoreFile)
			val android = project.extensions["android"] as BaseExtension
			val sign = SigningConfig(SIGNING_CONFIG_NAME).apply {
				storeFile = keyStoreFile
				storePassword = mandatoryProp(STORE_PASSWORD)
				keyAlias = optionalProp(KEY_ALIAS) ?: DEFAULT_KEY_ALIAS
				keyPassword = mandatoryProp(KEY_PASSWORD)
			}
			//android.signingConfigs.add(sign)
			android.buildTypes["release"].signingConfig = sign
		} else if (project.hasProperty(STORE_FILE)) {
			LOG.error("Keystore file (from {}) '{}' is not valid.", STORE_FILE, keyStoreFile.absolutePath)
		}
	}

	private fun optionalProp(name: String): String? = project.properties[name] as String?
	private fun mandatoryProp(name: String): String = optionalProp(name)!!
}
