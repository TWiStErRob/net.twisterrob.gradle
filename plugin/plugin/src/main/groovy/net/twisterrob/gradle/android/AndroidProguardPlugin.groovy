package net.twisterrob.gradle.android

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.BaseVariant
import com.android.builder.core.DefaultBuildType
import com.android.builder.model.AndroidProject
import net.twisterrob.gradle.Utils
import net.twisterrob.gradle.common.BasePlugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin

import static org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME

class AndroidProguardPlugin extends BasePlugin {
	@Override
	void apply(Project target) {
		super.apply(target)

		BaseExtension android = project.android
		File myProguardRules = new File(
				"${project.buildDir}/${AndroidProject.FD_INTERMEDIATES}/proguard/twisterrob.pro")
		android.with {
			DefaultBuildType release = buildTypes.release
			release.setRunProguard(true)
			release.proguardFiles.add android.getDefaultProguardFile('proguard-android.txt')
			release.proguardFiles.add myProguardRules
		}

		project.task('extractProguardRules') {
			description = "Extract proguard file from 'net.twisterrob.android' plugin"
			outputs.file(myProguardRules)
			doLast {
				new FileOutputStream(myProguardRules).withStream { outFile ->
					AndroidProguardPlugin.classLoader.getResourceAsStream("twisterrob.pro").withStream { inFile ->
						outFile << inFile
					}
				}
			}
		}

		project.afterEvaluate {
			Utils.getVariants(android).all { BaseVariant variant ->
				if (variant.obfuscation) {
					variant.obfuscation.dependsOn variant.obfuscation.project.tasks.extractProguardRules
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
	def tryAdd(File srcDir) {
		File proguardFile = new File(srcDir, 'proguard.pro');
		if (proguardFile.exists()) {
			project.android.buildTypes.release.proguardFiles.add proguardFile
		}
	}
}
