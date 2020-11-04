package net.twisterrob.gradle.android

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.tasks.ProguardTask
import com.android.builder.model.AndroidProject
import net.twisterrob.gradle.base.BasePlugin
import net.twisterrob.gradle.builtDate
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.task
import org.gradle.kotlin.dsl.withType
import org.intellij.lang.annotations.Language
import java.io.File

class AndroidProguardPlugin : BasePlugin() {

	override fun apply(target: Project) {
		super.apply(target)

		val android = project.extensions["android"] as BaseExtension

		/**
		 * @see com.android.build.gradle.ProguardFiles#getDefaultProguardFile
		 */
		val proguardBase = project.buildDir.resolve(AndroidProject.FD_INTERMEDIATES).resolve("proguard-rules")
		// TODO review ExtractProguardFiles task's files
		val defaultAndroidRules = proguardBase.resolve("android.pro")
		val myProguardRules = proguardBase.resolve("twisterrob.pro")
		val myDebugProguardRules = proguardBase.resolve("twisterrob-debug.pro")
		val myReleaseProguardRules = proguardBase.resolve("twisterrob-release.pro")
		val generatedProguardRulesFile = proguardBase.resolve("generated.pro")

		android.apply {
			defaultConfig.proguardFiles.add(generatedProguardRulesFile)
			defaultConfig.proguardFiles.add(defaultAndroidRules)
			defaultConfig.proguardFiles.add(myProguardRules)

			project.plugins.withType<AppPlugin> {
				val release = buildTypes["release"]
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
				val obfuscationTask = project.tasks
					.withType(ProguardTask::class.java)
					.matching { it.variantName == variant.name }
					.singleOrNull()
				if (obfuscationTask != null) {
					obfuscationTask.dependsOn(extractProguardRules)
					obfuscationTask.dependsOn(createGenerateProguardRulesTask(variant, generatedProguardRulesFile))
				}
			}
		}
	}

	private fun createGenerateProguardRulesTask(variant: BaseVariant, outputFile: File): Task =
		project.task<Task>("generate${variant.name.capitalize()}ProguardRules") {
			description = "Generates printConfiguration and dump options for ProGoard"
			val mappingFolder: Provider<File> = variant.mappingFileProvider.map { it.singleFile.parentFile }
			inputs.property("targetFolder", mappingFolder)
			outputs.file(outputFile)
			doFirst {
				@Language("proguard")
				val proguard = """
					-printconfiguration ${mappingFolder.get().resolve("configuration.pro")}
					-dump ${mappingFolder.get().resolve("dump.txt")}
				""".trimIndent()
				outputFile.writeText(proguard)
			}
		}

	private fun copy(internalName: String, targetFile: File) {
		targetFile.parentFile.mkdirs()
		val classLoader = this::class.java.classLoader!!
		classLoader.getResourceAsStream(internalName).copyTo(targetFile.outputStream())
		targetFile.setLastModified(builtDate.toEpochMilli())
	}
}
