package net.twisterrob.gradle.android

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.dsl.SigningConfig
import net.twisterrob.gradle.common.BasePlugin
import net.twisterrob.gradle.ext.forUseAtConfigurationTimeCompat
import org.gradle.api.Project
import org.gradle.kotlin.dsl.get

@Suppress("detekt.UnnecessaryAbstractClass") // Gradle convention.
abstract class AndroidSigningPlugin : BasePlugin() {

	override fun apply(target: Project) {
		super.apply(target)

		val keyStoreFile = project.file(optionalProp(STORE_FILE) ?: DEFAULT_STORE_FILE)
		if (keyStoreFile.isFile && keyStoreFile.exists() && keyStoreFile.canRead()) {
			LOG.info("Attaching release.signingConfig.{} using '{}'", SIGNING_CONFIG_NAME, keyStoreFile)
			val android = project.extensions["android"] as BaseExtension
			val sign: SigningConfig = android.signingConfigs.create(SIGNING_CONFIG_NAME).apply {
				setStoreFile(keyStoreFile)
				setStorePassword(mandatoryProp(STORE_PASSWORD))
				setKeyAlias(optionalProp(KEY_ALIAS) ?: DEFAULT_KEY_ALIAS)
				setKeyPassword(mandatoryProp(KEY_PASSWORD))
			}
			//android.signingConfigs.add(sign)
			// Use the oldest available overload that exists since AGP 3.1.4 through AGP 7.0.2:
			// fun setSigningConfig(signingConfig: com.android.builder.model.SigningConfig?): com.android.builder.model.BuildType
			android.buildTypes["release"].setSigningConfig(sign as com.android.builder.model.SigningConfig)
		} else if (project.providers.gradleProperty(STORE_FILE).forUseAtConfigurationTimeCompat().isPresent) {
			LOG.error("Keystore file (from {}) '{}' is not valid.", STORE_FILE, keyStoreFile.absolutePath)
		}
	}

	@Suppress("detekt.CastToNullableType") // Map<String, ?> -> it could be anything, it could be a null too.
	@Throws(ClassCastException::class) // If property is not a String.
	private fun optionalProp(name: String): String? =
		project.providers.gradleProperty(name).forUseAtConfigurationTimeCompat().orNull

	@Throws(ClassCastException::class) // If property is not a String.
	private fun mandatoryProp(name: String): String =
		requireNotNull(optionalProp(name)) { "Missing property '${name}'." }

	companion object {

		private const val SIGNING_CONFIG_NAME = "twisterrob"

		private const val DEFAULT_STORE_FILE = "twisterrob.jks"
		private const val STORE_FILE = "RELEASE_STORE_FILE"
		private const val STORE_PASSWORD = "RELEASE_STORE_PASSWORD"

		private const val DEFAULT_KEY_ALIAS = "net.twisterrob"
		private const val KEY_ALIAS = "RELEASE_KEY_ALIAS"
		private const val KEY_PASSWORD = "RELEASE_KEY_PASSWORD"
	}
}
