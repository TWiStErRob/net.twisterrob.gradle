package net.twisterrob.gradle.android

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.internal.lint.AndroidLintAnalysisTask
import com.android.build.gradle.internal.lint.AndroidLintGlobalTask
import com.android.build.gradle.internal.tasks.ProguardConfigurableTask
import com.android.build.gradle.internal.tasks.R8Task
import com.android.builder.model.AndroidProject
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
		val proguardBase = project.buildDir.resolve(AndroidProject.FD_INTERMEDIATES).resolve("proguard-rules")
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

			project.afterEvaluate {
				buildTypes.forEach { buildType ->
					if (buildType.isDebuggable) {
						buildType.proguardFiles.add(myDebugProguardRulesFile)
					} else {
						buildType.proguardFiles.add(myReleaseProguardRulesFile)
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
				copy("android.pro", defaultAndroidRulesFile)
				copy("twisterrob.pro", myProguardRulesFile)
				copy("twisterrob-debug.pro", myDebugProguardRulesFile)
				copy("twisterrob-release.pro", myReleaseProguardRulesFile)
			}
		}

		lintTasksDependOnProguardRulesTask(extractMinificationRules)
		project.afterEvaluate {
			android.variants.all { variant ->
				val proguardTask = proguardTaskClass?.let { project.findMinificationTaskFor(variant, it) }
				val r8Task = project.findMinificationTaskFor(variant, R8Task::class.java)
				val obfuscationTask = proguardTask ?: r8Task
				if (obfuscationTask != null) {
					obfuscationTask.dependsOn(extractMinificationRules)
					val generateMinificationRulesTask = createGenerateMinificationRulesTask(
						variant,
						generatedProguardRulesFile,
						proguardTask == obfuscationTask
					)
					lintTasksDependOnProguardRulesTask(generateMinificationRulesTask)
					obfuscationTask.dependsOn(generateMinificationRulesTask)
				}
			}
		}
	}

	private fun lintTasksDependOnProguardRulesTask(task: TaskProvider<Task>) {
		if (AGPVersions.CLASSPATH >= AGPVersions.v70x) {
			// REPORT allow tasks to generate ProGuard files, this must be possible because aapt generates one.
			project.tasks.withType<AndroidLintGlobalTask>().configureEach { it.dependsOn(task) }
			project.tasks.withType<AndroidLintAnalysisTask>().configureEach { it.dependsOn(task) }
		}
	}

	/**
	 * AGP 7 fully removed support for this task. This will only return a value in < [AGPVersions.v70x].
	 */
	@Suppress("UNCHECKED_CAST")
	private val proguardTaskClass: Class<out ProguardConfigurableTask>?
		get() = try {
			Class.forName("com.android.build.gradle.internal.tasks.ProguardTask")
					as Class<out ProguardConfigurableTask>
		} catch (ex: ClassNotFoundException) {
			null
		}

	private fun <T : ProguardConfigurableTask> Project.findMinificationTaskFor(
		variant: @Suppress("DEPRECATION" /* AGP 7.0 */) com.android.build.gradle.api.BaseVariant,
		taskClass: Class<T>
	): T? =
		this
			.tasks
			.withType(taskClass)
			.matching { it.variantName == variant.name }
			.singleOrNull()

	private fun createGenerateMinificationRulesTask(
		variant: @Suppress("DEPRECATION" /* AGP 7.0 */) com.android.build.gradle.api.BaseVariant,
		outputFile: File,
		isProguard: Boolean
	): TaskProvider<Task> =
		project.tasks.register<Task>("generate${variant.name.capitalize()}MinificationRules") {
			description = "Generates printConfiguration and dump options for ProGuard or R8"
			val mappingFolder: Provider<File> = variant.mappingFileProvider.map { it.singleFile.parentFile }
			inputs.property("targetFolder", mappingFolder)
			outputs.file(outputFile)
			doFirst {
				outputFile.createNewFile()
				if (isProguard || AGPVersions.CLASSPATH < AGPVersions.v41x) {
					outputFile.appendText("-printconfiguration ${mappingFolder.get().resolve("configuration.txt")}\n")
				}
				if (isProguard) {
					outputFile.appendText("-dump ${mappingFolder.get().resolve("dump.txt")}\n")
				}
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
