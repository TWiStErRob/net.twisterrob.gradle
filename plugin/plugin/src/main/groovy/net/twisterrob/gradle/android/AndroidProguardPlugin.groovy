package net.twisterrob.gradle.android

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.pipeline.TransformTask
import com.android.build.gradle.internal.transforms.ProGuardTransform
import com.android.builder.core.DefaultBuildType
import com.android.builder.model.AndroidProject
import net.twisterrob.gradle.Utils
import net.twisterrob.gradle.common.BasePlugin
import org.gradle.api.Project

import static org.gradle.api.tasks.SourceSet.*

class AndroidProguardPlugin extends BasePlugin {
	@Override
	void apply(Project target) {
		super.apply(target)

		BaseExtension android = project.android
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
			Utils.getVariants(android).all { BaseVariant variant ->
				def obfuscation = variant.variantData.mappingFileProviderTask
				if (obfuscation) {
					def task = obfuscation.task as TransformTask;
					def proguard = task.transform as ProGuardTransform
					task.dependsOn extractProguardRules
					proguard.printconfiguration(new File(variant.mappingFile.parentFile, 'configuration.pro'))
					task.doFirst {
						proguard.secondaryFileInputs.each { println "ProGuard configuration file: $it" }
					}
				}
			}

			project.rootProject.allprojects.each { Project subProject ->
				subProject.plugins.withType(com.android.build.gradle.AppPlugin) {
					subProject.android.sourceSets[MAIN_SOURCE_SET_NAME]
					          .java.srcDirs.each { File srcDir -> tryAdd(srcDir) }
				}
				subProject.plugins.withType(com.android.build.gradle.LibraryPlugin) {
					subProject.android.sourceSets[MAIN_SOURCE_SET_NAME]
					          .java.srcDirs.each { File srcDir -> tryAdd(srcDir) }
				}
				subProject.plugins.withType(org.gradle.api.plugins.JavaPlugin) {
					subProject.sourceSets[MAIN_SOURCE_SET_NAME]
					          .java.srcDirs.each { File srcDir -> tryAdd(srcDir) }
				}
			}
		}
	}

	private void copy(String internalName, File targetFile) {
		targetFile.getParentFile().mkdirs();
		new FileOutputStream(targetFile).withStream { outFile ->
			AndroidProguardPlugin.classLoader.getResourceAsStream(internalName).withStream { inFile ->
				outFile << inFile
			}
		}
		targetFile.setLastModified(this.builtDate.time)
	}
	def tryAdd(File srcDir) {
		File proguardFile = new File(srcDir, 'proguard.pro');
		if (proguardFile.exists()) {
			project.android.defaultConfig.proguardFiles.add proguardFile
		}
	}
}
