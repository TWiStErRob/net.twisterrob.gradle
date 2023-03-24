package net.twisterrob.gradle.android

import com.android.SdkConstants
import com.android.build.api.variant.Variant
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.internal.lint.AndroidLintAnalysisTask
import com.android.build.gradle.internal.lint.AndroidLintGlobalTask
import com.android.build.gradle.internal.lint.LintModelWriterTask
import com.android.build.gradle.internal.tasks.R8Task
import net.twisterrob.gradle.common.BasePlugin
import org.gradle.api.Project
import org.gradle.api.Task
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

		android.apply {
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
			setupAutoProguardFiles()
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

		lintDependsOnGenerateRulesTask(extractMinificationRules)
		project.androidComponents.onVariantsCompat { variant ->
			generateVariantRules(variant, extractMinificationRules)
		}
	}

	private fun BaseExtension.setupAutoProguardFiles() {
		val autoProguardFile = project.file("src/main/proguard.pro")
		if (autoProguardFile.exists() && autoProguardFile.isFile) {
			defaultConfig.proguardFiles.add(autoProguardFile)
		}
		val autoDexMainFile = project.file("src/main/multidex.pro")
		if (autoDexMainFile.exists() && autoDexMainFile.isFile) {
			defaultConfig.multiDexKeepProguard = autoDexMainFile
		}
		project.plugins.withType<LibraryPlugin> {
			val autoConsumerFile = project.file("src/main/consumer.pro")
			if (autoConsumerFile.exists() && autoConsumerFile.isFile) {
				defaultConfig.consumerProguardFiles(autoConsumerFile)
			}
		}
	}

	private fun generateVariantRules(variant: Variant, extractMinificationRules: TaskProvider<Task>) {
		project.afterEvaluate { project ->
			project.tasks.withType<R8Task>().configureEach { obfuscationTask ->
				if (obfuscationTask.variantName == variant.name) {
					obfuscationTask.dependsOn(extractMinificationRules)
				}
			}
		}
	}

	private fun lintDependsOnGenerateRulesTask(task: TaskProvider<Task>) {
		// REPORT allow tasks to generate ProGuard files, this must be possible because aapt generates one.
		project.tasks.withType<AndroidLintGlobalTask>().configureEach { it.mustRunAfter(task) }
		project.tasks.withType<AndroidLintAnalysisTask>().configureEach { it.mustRunAfter(task) }
		project.tasks.withType<LintModelWriterTask>().configureEach { it.mustRunAfter(task) }
	}

	private fun copy(internalName: String, targetFile: File) {
		targetFile.parentFile.mkdirs()
		val resource = this::class.java.getResourceAsStream(internalName)
			?: error("Cannot find ${internalName} to copy to ${targetFile}.")
		resource.use { inp -> targetFile.outputStream().use { out -> inp.copyTo(out) } }
		targetFile.setLastModified(builtDate.toEpochMilli())
	}
}
