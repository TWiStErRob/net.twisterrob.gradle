package net.twisterrob.gradle.android

import com.android.SdkConstants
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.internal.lint.AndroidLintAnalysisTask
import com.android.build.gradle.internal.lint.AndroidLintGlobalTask
import com.android.build.gradle.internal.tasks.ProguardConfigurableTask
import com.android.build.gradle.internal.tasks.R8Task
import net.twisterrob.gradle.base.BasePlugin
import net.twisterrob.gradle.common.AGPVersions
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import java.io.File

class AndroidMinificationPlugin : BasePlugin() {

	override fun apply(target: Project) {
		super.apply(target)

		val android = project.extensions["android"] as BaseExtension

		/**
		 * @see com.android.build.gradle.ProguardFiles#getDefaultProguardFile
		 */
		val proguardBase = project.buildDir.resolve(SdkConstants.FD_INTERMEDIATES).resolve("proguard-rules")
		// TODO review ExtractProguardFiles task's files
		val defaultAndroidRulesFile = proguardBase.resolve("android.pro")
		val myProguardRulesFile = proguardBase.resolve("twisterrob.pro")
		val myDebugProguardRulesFile = proguardBase.resolve("twisterrob-debug.pro")
		val myReleaseProguardRulesFile = proguardBase.resolve("twisterrob-release.pro")
		val generatedProguardRulesFile = proguardBase.resolve("generated.pro")

		android.apply {
			defaultConfig.proguardFiles.add(generatedProguardRulesFile)
			defaultConfig.proguardFiles.add(defaultAndroidRulesFile)
			defaultConfig.proguardFiles.add(myProguardRulesFile)

			project.plugins.withType<AppPlugin> {
				val release = buildTypes["release"]
				release.setMinifyEnabled(true)
			}

			buildTypes.all { buildType ->
				if (buildType.isDebuggable) {
					buildType.proguardFiles.add(myDebugProguardRulesFile)
				} else {
					buildType.proguardFiles.add(myReleaseProguardRulesFile)
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

		val extractMinificationRules = project.tasks.register<Task>("extractMinificationRules") {
			description = "Extract ProGuard files from 'net.twisterrob.android' plugin"
			outputs.file(defaultAndroidRulesFile)
			outputs.file(myProguardRulesFile)
			outputs.file(myDebugProguardRulesFile)
			outputs.file(myReleaseProguardRulesFile)
			outputs.upToDateWhen {
				defaultAndroidRulesFile.lastModified() == builtDate.toEpochMilli()
						&& myProguardRulesFile.lastModified() == builtDate.toEpochMilli()
						&& myDebugProguardRulesFile.lastModified() == builtDate.toEpochMilli()
						&& myReleaseProguardRulesFile.lastModified() == builtDate.toEpochMilli()
			}
			doLast {
				copy("/android.pro", defaultAndroidRulesFile)
				copy("/twisterrob.pro", myProguardRulesFile)
				copy("/twisterrob-debug.pro", myDebugProguardRulesFile)
				copy("/twisterrob-release.pro", myReleaseProguardRulesFile)
			}
		}

		lintTasksDependOnProguardRulesTask(extractMinificationRules)
		project.afterEvaluate {
			android.variants.configureEach { variant ->
				val isFlavorless = variant.flavorName == ""
				if (!isFlavorless) {
					// Cannot do this simply because AGP doesn't provide a DSL surface for adding proguard rules for variants.
					// It's possible to add for buildType and flavors, but since the mapping file is for variants,
					// the generate... task would not be able to create a distinct file.
					project.logger.warn("This project uses flavors, it's not possible to generate variant based rules for ${variant.name}.")
				}
				val generateProguardRulesTask =
					createGenerateProguardMinificationRulesTask(variant, generatedProguardRulesFile)
				if (isFlavorless) {
					lintTasksDependOnProguardRulesTask(generateProguardRulesTask)
				}
				proguardTaskClass()
					?.let { project.tasks.withType(it) }
					?.configureEach { obfuscationTask ->
						if (obfuscationTask.variantName == variant.name) {
							obfuscationTask.dependsOn(extractMinificationRules)
							if (isFlavorless) {
								obfuscationTask.dependsOn(generateProguardRulesTask)
							}
						}
					}
				val generateR8RulesTask =
					createGenerateR8MinificationRulesTask(variant, generatedProguardRulesFile)
				if (isFlavorless) {
					lintTasksDependOnProguardRulesTask(generateR8RulesTask)
				}
				project.tasks.withType<R8Task>().configureEach { obfuscationTask ->
					if (obfuscationTask.variantName == variant.name) {
						obfuscationTask.dependsOn(extractMinificationRules)
						if (isFlavorless) {
							obfuscationTask.dependsOn(generateR8RulesTask)
						}
					}
				}
			}
		}
	}

	private fun lintTasksDependOnProguardRulesTask(task: TaskProvider<Task>) {
		if (AGPVersions.CLASSPATH >= AGPVersions.v70x) {
			// REPORT allow tasks to generate ProGuard files, this must be possible because aapt generates one.
			project.tasks.withType<AndroidLintGlobalTask>().configureEach { it.mustRunAfter(task) }
			project.tasks.withType<AndroidLintAnalysisTask>().configureEach { it.mustRunAfter(task) }
		}
	}

	/**
	 * AGP 7 fully removed support for this task. This will only return a value in < [AGPVersions.v70x].
	 */
	private fun proguardTaskClass(): Class<out ProguardConfigurableTask>? =
		try {
			@Suppress("UNCHECKED_CAST")
			Class.forName("com.android.build.gradle.internal.tasks.ProguardTask")
					as Class<out ProguardConfigurableTask>
		} catch (ignore: ClassNotFoundException) {
			null
		}

	/**
	 * Duplicate code, see also [createGenerateProguardMinificationRulesTask].
	 */
	private fun createGenerateR8MinificationRulesTask(
		variant: @Suppress("DEPRECATION" /* AGP 7.0 */) com.android.build.gradle.api.BaseVariant,
		outputFile: File,
	): TaskProvider<Task> =
		project.tasks.register<Task>("generate${variant.name.capitalize()}R8MinificationRules") {
			description = "Generates printConfiguration for R8 if supported."
			val mappingFolder: Provider<File> = variant.mappingFileProvider.map { it.singleFile.parentFile }
			inputs.property("targetFolder", mappingFolder)
			outputs.file(outputFile)
			doFirst {
				outputFile.parentFile.mkdirs()
				outputFile.createNewFile()
				if (AGPVersions.CLASSPATH < AGPVersions.v41x) {
					outputFile.appendText("-printconfiguration ${mappingFolder.get().resolve("configuration.txt")}\n")
				}
			}
		}

	/**
	 * Duplicate code, see also [createGenerateR8MinificationRulesTask].
	 */
	private fun createGenerateProguardMinificationRulesTask(
		variant: @Suppress("DEPRECATION" /* AGP 7.0 */) com.android.build.gradle.api.BaseVariant,
		outputFile: File,
	): TaskProvider<Task> =
		project.tasks.register<Task>("generate${variant.name.capitalize()}ProguardMinificationRules") {
			description = "Generates printConfiguration and dump options for ProGuard."
			val mappingFolder: Provider<File> = variant.mappingFileProvider.map { it.singleFile.parentFile }
			inputs.property("targetFolder", mappingFolder)
			outputs.file(outputFile)
			doFirst {
				outputFile.parentFile.mkdirs()
				outputFile.createNewFile()
				outputFile.writeText(
					"""
						-printconfiguration ${mappingFolder.get().resolve("configuration.txt")}
						-dump ${mappingFolder.get().resolve("dump.txt")}
					""".trimIndent()
				)
			}
		}

	private fun copy(internalName: String, targetFile: File) {
		targetFile.parentFile.mkdirs()
		val resource = this::class.java.getResourceAsStream(internalName)
			?: error("Cannot find ${internalName} to copy to ${targetFile}.")
		resource.use { inp -> targetFile.outputStream().use { out -> inp.copyTo(out) } }
		targetFile.setLastModified(builtDate.toEpochMilli())
	}
}
