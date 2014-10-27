package net.twisterrob.gradle.android

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.dsl.SigningConfigDsl
import org.gradle.api.*

/**
 * Created by TWiStEr on 2014-10-02.
 */
class AndroidSigningPlugin implements Plugin<Project> {
	@Override
	void apply(Project project) {
		File keyStoreFile = project.file(project.properties['RELEASE_STORE_FILE'] ?: 'twisterrob.jks')
		if (keyStoreFile.isFile() && keyStoreFile.exists() && keyStoreFile.canRead()) {
			BaseExtension android = project.android
			android.with {
				def sign = new SigningConfigDsl("twisterrob")
				sign.with {
					storeFile = keyStoreFile
					storePassword = project.properties['RELEASE_STORE_PASSWORD']
					keyAlias = project.properties['RELEASE_KEY_ALIAS'] ?: 'net.twisterrob'
					keyPassword = project.properties['RELEASE_KEY_PASSWORD']
				}
				//signingConfigs.add(sign)
				buildTypes.release.signingConfig = sign
			}
		} else if (project.properties['RELEASE_STORE_FILE']) {
			println "Keystore file '${keyStoreFile.absolutePath}' is not valid."
		}
	}
}
