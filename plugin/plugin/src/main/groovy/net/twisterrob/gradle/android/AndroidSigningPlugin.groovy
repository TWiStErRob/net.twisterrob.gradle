package net.twisterrob.gradle.android

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.dsl.SigningConfig
import net.twisterrob.gradle.common.BasePlugin
import org.gradle.api.Project

class AndroidSigningPlugin extends BasePlugin {
	private static final String SIGNING_CONFIG_NAME = "twisterrob"

	@Override
	void apply(Project target) {
		super.apply(target)

		File keyStoreFile = project.file(project.properties['RELEASE_STORE_FILE'] ?: 'twisterrob.jks')
		if (keyStoreFile.isFile() && keyStoreFile.exists() && keyStoreFile.canRead()) {
			LOG.info('Attaching release.signingConfig.{} using "{}"', SIGNING_CONFIG_NAME, keyStoreFile)
			BaseExtension android = project.android
			android.with {
				// TODO consider using com.android.builder.model.AndroidProject.PROPERTY_SIGNING_STORE_FILE
				def sign = new SigningConfig(SIGNING_CONFIG_NAME)
				sign.with {
					storeFile = keyStoreFile
					storePassword = project.properties['RELEASE_STORE_PASSWORD']
					keyAlias = project.properties['RELEASE_KEY_ALIAS'] ?: 'net.twisterrob'
					keyPassword = project.properties['RELEASE_KEY_PASSWORD']
				}
				//signingConfigs.add(sign)
				buildTypes['release'].setSigningConfig sign
			}
		} else if (project.hasProperty('RELEASE_STORE_FILE')) {
			LOG.error('Keystore file "{}" is not valid.', keyStoreFile.absolutePath)
		}
	}
}
