package net.twisterrob.gradle.android

import com.android.SdkConstants
import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.Variant
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.internal.lint.AndroidLintAnalysisTask
import com.android.build.gradle.internal.lint.AndroidLintGlobalTask
import com.android.build.gradle.internal.lint.LintModelWriterTask
import com.android.build.gradle.internal.tasks.R8Task
import net.twisterrob.gradle.common.BasePlugin
import net.twisterrob.gradle.kotlin.dsl.withId
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

@Suppress("detekt.UnnecessaryAbstractClass") // Gradle convention.
abstract class AndroidMinificationPlugin : BasePlugin() {

	override fun apply(target: Project) {
		super.apply(target)

		val android = project.extensions["android"] as BaseExtension

		/**
		 * @see com.android.build.gradle.ProguardFiles#getDefaultProguardFile
		 */
		val proguardBase = project.layout.buildDirectory
			.dir(SdkConstants.FD_INTERMEDIATES)
			.map { it.dir("proguard-rules") }
		// TODO review ExtractProguardFiles task's files
		val defaultAndroidRulesFile = proguardBase.map { it.file("android.pro") }
		val myProguardRulesFile = proguardBase.map { it.file("twisterrob.pro") }
		val myDebugProguardRulesFile = proguardBase.map { it.file("twisterrob-debug.pro") }
		val myReleaseProguardRulesFile = proguardBase.map { it.file("twisterrob-release.pro") }

		android.apply {
			defaultConfig.proguardFile(defaultAndroidRulesFile)
			defaultConfig.proguardFile(myProguardRulesFile)

			project.plugins.withId<AppPlugin>("com.android.application") {
				this@apply as ApplicationExtension
				buildTypes.configure("release") {
					it.isMinifyEnabled = true
				}
				buildTypes.configureEach { buildType ->
					if (buildType.isDebuggable) {
						buildType.proguardFile(myDebugProguardRulesFile)
					} else {
						buildType.proguardFile(myReleaseProguardRulesFile)
					}
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
				defaultAndroidRulesFile.get().asFile.lastModified() == builtDate.toEpochMilli()
						&& myProguardRulesFile.get().asFile.lastModified() == builtDate.toEpochMilli()
						&& myDebugProguardRulesFile.get().asFile.lastModified() == builtDate.toEpochMilli()
						&& myReleaseProguardRulesFile.get().asFile.lastModified() == builtDate.toEpochMilli()
			}
			doLast {
				copy("/android.pro", defaultAndroidRulesFile)
				copy("/twisterrob.pro", myProguardRulesFile)
				copy("/twisterrob-debug.pro", myDebugProguardRulesFile)
				copy("/twisterrob-release.pro", myReleaseProguardRulesFile)
			}
		}

		lintDependsOnGenerateRulesTask(extractMinificationRules)
		project.androidComponents.onVariants { variant ->
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
		project.plugins.withId<LibraryPlugin>("com.android.library") {
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

	companion object {
		private fun copy(internalName: String, target: Provider<RegularFile>) {
			val targetFile = target.get().asFile
			targetFile.parentFile.mkdirs()
			val resource = AndroidMinificationPlugin::class.java.getResourceAsStream(internalName)
				?: error("Cannot find ${internalName} to copy to ${targetFile}.")
			resource.use { inp -> targetFile.outputStream().use { out -> inp.copyTo(out) } }
			targetFile.setLastModified(builtDate.toEpochMilli())
		}
	}
}
