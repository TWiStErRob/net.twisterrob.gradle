package net.twisterrob.gradle.android

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.api.BaseVariantImpl
import com.android.build.gradle.internal.pipeline.TransformTask
import com.android.build.gradle.internal.transforms.ProGuardTransform
import com.android.builder.core.DefaultBuildType
import com.android.builder.model.AndroidProject
import net.twisterrob.gradle.Utils
import net.twisterrob.gradle.common.BasePlugin
import org.gradle.api.Project

class AndroidProguardPlugin extends BasePlugin {

	@Override
	void apply(Project target) {
		super.apply(target)

		BaseExtension android = project.android
		/**
		 * @see com.android.build.gradle.ProguardFiles#getDefaultProguardFile
		 */
		File proguardBase = new File("${project.buildDir}/${AndroidProject.FD_INTERMEDIATES}/proguard-rules")
		File defaultAndroidRules = new File(proguardBase, "android.pro")
		File myProguardRules = new File(proguardBase, "twisterrob.pro")
		File myDebugProguardRules = new File(proguardBase, "twisterrob-debug.pro")
		File myReleaseProguardRules = new File(proguardBase, "twisterrob-release.pro")
		android.with {
			defaultConfig.proguardFiles.add defaultAndroidRules
			defaultConfig.proguardFiles.add myProguardRules

			DefaultBuildType release = buildTypes['release'] as DefaultBuildType
			release.setMinifyEnabled(true)

			project.afterEvaluate {
				buildTypes.each { buildType ->
					if (buildType.debuggable) {
						buildType.proguardFiles.add myDebugProguardRules
					} else {
						buildType.proguardFiles.add myReleaseProguardRules
					}
				}
			}
		}

		def extractProguardRules = project.task('extractProguardRules') {
			description = "Extract proguard file from 'net.twisterrob.android' plugin"
			outputs.files defaultAndroidRules, myProguardRules
			outputs.upToDateWhen {
				defaultAndroidRules.lastModified() == this.builtDate.time \
				  && myProguardRules.lastModified() == this.builtDate.time \
				  && myDebugProguardRules.lastModified() == this.builtDate.time \
				  && myReleaseProguardRules.lastModified() == this.builtDate.time
			}
			doLast {
				copy("android.pro", defaultAndroidRules)
				copy("twisterrob.pro", myProguardRules)
				copy("twisterrob-debug.pro", myDebugProguardRules)
				copy("twisterrob-release.pro", myReleaseProguardRules)
			}
		}

		project.afterEvaluate {
			Utils.getVariants(android).all { BaseVariantImpl variant ->
				TransformTask obfuscationTask = project.tasks.matching {
					it instanceof TransformTask \
					  && it.variantName == variant.name \
					  && it.transform instanceof ProGuardTransform
				}.find() as TransformTask
				if (obfuscationTask) {
					obfuscationTask.dependsOn extractProguardRules
					def proguard = obfuscationTask.transform as ProGuardTransform
					proguard.printconfiguration(new File(variant.mappingFile.parentFile, 'configuration.pro'))
				}
			}
		}
	}

	private void copy(String internalName, File targetFile) {
		targetFile.getParentFile().mkdirs()
		new FileOutputStream(targetFile).withStream { outFile ->
			AndroidProguardPlugin.classLoader.getResourceAsStream(internalName).withStream { inFile ->
				outFile << inFile
			}
		}
		targetFile.setLastModified(this.builtDate.time)
	}
}
