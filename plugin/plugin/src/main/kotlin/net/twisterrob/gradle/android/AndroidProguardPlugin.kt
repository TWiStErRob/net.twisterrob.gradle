package net.twisterrob.gradle.android

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.internal.pipeline.TransformTask
import com.android.build.gradle.internal.transforms.ProGuardTransform
import com.android.build.gradle.internal.transforms.configuration
import com.android.builder.core.DefaultBuildType
import com.android.builder.model.AndroidProject
import net.twisterrob.gradle.base.BasePlugin
import net.twisterrob.gradle.builtDate
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.task
import org.gradle.kotlin.dsl.withType
import java.io.File

class AndroidProguardPlugin : BasePlugin() {

	override fun apply(target: Project) {
		super.apply(target)

		val android = project.extensions["android"] as BaseExtension
		/**
		 * @see com.android.build.gradle.ProguardFiles#getDefaultProguardFile
		 */
		val proguardBase = File("${project.buildDir}/${AndroidProject.FD_INTERMEDIATES}/proguard-rules")
		val defaultAndroidRules = File(proguardBase, "android.pro")
		val myProguardRules = File(proguardBase, "twisterrob.pro")
		val myDebugProguardRules = File(proguardBase, "twisterrob-debug.pro")
		val myReleaseProguardRules = File(proguardBase, "twisterrob-release.pro")
		android.apply {
			defaultConfig.proguardFiles.add(defaultAndroidRules)
			defaultConfig.proguardFiles.add(myProguardRules)

			project.plugins.withType<AppPlugin> {
				val release = buildTypes["release"] as DefaultBuildType
				release.isMinifyEnabled = true
			}

			project.afterEvaluate {
				buildTypes.forEach { buildType ->
					if (buildType.isDebuggable) {
						buildType.proguardFiles.add(myDebugProguardRules)
					} else {
						buildType.proguardFiles.add(myReleaseProguardRules)
					}
				}
			}

			val autoProguardFile = project.file("src/main/proguard.pro")
			if (autoProguardFile.exists() && autoProguardFile.isFile) {
				android.defaultConfig.proguardFiles.add(autoProguardFile)
			}
			val autoDexMainFile = project.file("src/main/multidex.pro")
			if (autoDexMainFile.exists() && autoDexMainFile.isFile) {
				android.defaultConfig.multiDexKeepProguard = autoDexMainFile
			}
			project.plugins.withType<LibraryPlugin> {
				val autoConsumerFile = project.file("src/main/consumer.pro")
				if (autoConsumerFile.exists() && autoConsumerFile.isFile) {
					android.defaultConfig.consumerProguardFiles(autoConsumerFile)
				}
			}
		}

		val extractProguardRules = project.task<Task>("extractProguardRules") {
			description = "Extract proguard file from 'net.twisterrob.android' plugin"
			outputs.files(defaultAndroidRules, myProguardRules)
			outputs.upToDateWhen {
				defaultAndroidRules.lastModified() == builtDate.toEpochMilli()
						&& myProguardRules.lastModified() == builtDate.toEpochMilli()
						&& myDebugProguardRules.lastModified() == builtDate.toEpochMilli()
						&& myReleaseProguardRules.lastModified() == builtDate.toEpochMilli()
			}
			doLast {
				copy("android.pro", defaultAndroidRules)
				copy("twisterrob.pro", myProguardRules)
				copy("twisterrob-debug.pro", myDebugProguardRules)
				copy("twisterrob-release.pro", myReleaseProguardRules)
			}
		}

		project.afterEvaluate {
			android.variants.all { variant ->
				val obfuscationTask = project.tasks.matching { task ->
					task is TransformTask
							&& task.variantName == variant.name
							&& task.transform is ProGuardTransform
				}.singleOrNull() as TransformTask?
				if (obfuscationTask != null) {
					obfuscationTask.dependsOn(extractProguardRules)
					val proguard = obfuscationTask.transform as ProGuardTransform
					proguard.configuration.printConfiguration =
							File(variant.mappingFile.parentFile, "configuration.pro")
				}
			}
		}
	}

	private fun copy(internalName: String, targetFile: File) {
		targetFile.parentFile.mkdirs()
		val classLoader = this::class.java.classLoader!!
		classLoader.getResourceAsStream(internalName).copyTo(targetFile.outputStream())
		targetFile.setLastModified(builtDate.toEpochMilli())
	}
}
