package net.twisterrob.gradle.android

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.tasks.ProguardConfigurableTask
import com.android.build.gradle.internal.tasks.ProguardTask
import com.android.build.gradle.internal.tasks.R8Task
import com.android.builder.model.AndroidProject
import net.twisterrob.gradle.base.BasePlugin
import net.twisterrob.gradle.builtDate
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Provider
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

		val extractProguardRules = project.task<Task>("extractMinificationRules") {
			description = "Extract ProGuard files from 'net.twisterrob.android' plugin"
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
				val proguardTask = project.findMinificationTaskFor<ProguardTask>(variant)
				val r8Task = project.findMinificationTaskFor<R8Task>(variant)
				val obfuscationTask = proguardTask ?: r8Task
				if (obfuscationTask != null) {
					obfuscationTask.dependsOn(extractProguardRules)
					val generateMinificationRulesTask = createGenerateMinificationRulesTask(
						variant,
						generatedProguardRulesFile,
						proguardTask == obfuscationTask
					)
					obfuscationTask.dependsOn(generateMinificationRulesTask)
				}
			}
		}
	}

	private inline fun <reified T : ProguardConfigurableTask> Project.findMinificationTaskFor(variant: BaseVariant): T? =
		this
			.tasks
			.withType(T::class.java)
			.matching { it.variantName == variant.name }
			.singleOrNull()

	private fun createGenerateMinificationRulesTask(variant: BaseVariant, outputFile: File, isProguard: Boolean): Task =
		project.task<Task>("generate${variant.name.capitalize()}MinificationRules") {
			description = "Generates printConfiguration and dump options for ProGuard or R8"
			val mappingFolder: Provider<File> = variant.mappingFileProvider.map { it.singleFile.parentFile }
			inputs.property("targetFolder", mappingFolder)
			outputs.file(outputFile)
			doFirst {
				outputFile.createNewFile()
				if (isProguard) {
					outputFile.appendText("-printconfiguration ${mappingFolder.get().resolve("configuration.txt")}\n")
					outputFile.appendText("-dump ${mappingFolder.get().resolve("dump.txt")}\n")
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
