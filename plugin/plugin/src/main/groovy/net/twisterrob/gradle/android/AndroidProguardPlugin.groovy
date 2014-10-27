package net.twisterrob.gradle.android

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.BaseVariant
import com.android.builder.core.DefaultBuildType
import com.android.builder.model.AndroidProject
import net.twisterrob.gradle.Utils
import net.twisterrob.gradle.common.BasePlugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin

import static org.gradle.api.tasks.SourceSet.*

class AndroidProguardPlugin extends BasePlugin {
	@Override
	void apply(Project target) {
		super.apply(target)

		BaseExtension android = project.android
		File proguardBase = new File("${project.buildDir}/${AndroidProject.FD_INTERMEDIATES}/proguard")
		File defaultModifiedAndroidRules = new File(proguardBase, "android-project.txt")
		File myProguardRules = new File(proguardBase, "twisterrob.pro")
		android.with {
			DefaultBuildType release = buildTypes.release
			release.setRunProguard(true)
			release.proguardFiles.add defaultModifiedAndroidRules
			release.proguardFiles.add myProguardRules
		}

		def extractProguardRules = project.task('extractProguardRules') {
			description = "Extract proguard file from 'net.twisterrob.android' plugin"
			outputs.files defaultModifiedAndroidRules, myProguardRules
			doLast {
				copy("android.pro", defaultModifiedAndroidRules)
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
				if (variant.obfuscation) {
					variant.obfuscation.dependsOn extractProguardRules
				}
			}

			project.rootProject.allprojects.each { Project subProject ->
				subProject.plugins.withType(com.android.build.gradle.BasePlugin) {
					subProject.android.sourceSets[MAIN_SOURCE_SET_NAME]
					          .java.srcDirs.each { File srcDir -> tryAdd(srcDir) }
				}
				subProject.plugins.withType(JavaPlugin) {
					subProject.sourceSets[MAIN_SOURCE_SET_NAME]
					          .java.srcDirs.each { File srcDir -> tryAdd(srcDir) }
				}
			}
		}
	}
	private static void copy(String internalName, File targetFile) {
		new FileOutputStream(targetFile).withStream { outFile ->
			AndroidProguardPlugin.classLoader.getResourceAsStream(internalName).withStream { inFile ->
				outFile << inFile
			}
		}
	}
	def tryAdd(File srcDir) {
		File proguardFile = new File(srcDir, 'proguard.pro');
		if (proguardFile.exists()) {
			project.android.buildTypes.release.proguardFiles.add proguardFile
		}
	}
}
