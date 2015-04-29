package net.twisterrob.gradle.android

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.BaseVariant
import com.android.builder.core.DefaultBuildType
import com.android.builder.model.AndroidProject
import net.twisterrob.gradle.Utils
import net.twisterrob.gradle.common.BasePlugin
import org.gradle.api.Project
import proguard.gradle.ProGuardTask

import static org.gradle.api.tasks.SourceSet.*

class AndroidProguardPlugin extends BasePlugin {
	@Override
	void apply(Project target) {
		super.apply(target)

		BaseExtension android = project.android
		File proguardBase = new File("${project.buildDir}/${AndroidProject.FD_INTERMEDIATES}/proguard-rules")
		File defaultAndroidRules = new File(proguardBase, "android-project-mod.txt")
		File myProguardRules = new File(proguardBase, "twisterrob.pro")
		android.with {
			DefaultBuildType release = buildTypes.release
			release.setMinifyEnabled(true)
			release.proguardFiles.add defaultAndroidRules
			release.proguardFiles.add myProguardRules
		}

		def extractProguardRules = project.task('extractProguardRules') {
			description = "Extract proguard file from 'net.twisterrob.android' plugin"
			outputs.files defaultAndroidRules, myProguardRules
			outputs.upToDateWhen {
				defaultAndroidRules.lastModified() == this.builtDate.time && myProguardRules.lastModified() == this.builtDate.time
			}
			doLast {
				copy("android.pro", defaultAndroidRules)
				copy("twisterrob.pro", myProguardRules)
			}
		}

//		project.afterEvaluate {
//			tasks.proguardRelease.doFirst {
//				project.android.buildTypes.release.proguardFiles.each { println "Proguard configuration file: $it" }
//			}
//		}

		project.afterEvaluate {
			Utils.getVariants(android).all { BaseVariant variant ->
				ProGuardTask obfuscation = variant.obfuscation as ProGuardTask
				if (obfuscation) {
					obfuscation.dependsOn extractProguardRules
					obfuscation.printconfiguration(new File(variant.mappingFile.parentFile, 'configuration.pro'))
					if (!variant.buildType.debuggable) {
						obfuscation.renamesourcefileattribute("SourceFile")
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
			project.android.buildTypes.release.proguardFiles.add proguardFile
		}
	}
}
