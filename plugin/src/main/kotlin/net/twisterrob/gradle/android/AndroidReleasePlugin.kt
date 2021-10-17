package net.twisterrob.gradle.android

import com.android.build.api.variant.impl.BuiltArtifactsImpl
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.internal.api.TestedVariant
import com.android.builder.model.BuildType
import com.android.builder.model.ProductFlavor
import net.twisterrob.gradle.base.BasePlugin
import net.twisterrob.gradle.kotlin.dsl.extensions
import org.gradle.api.DomainObjectCollection
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.UnknownTaskException
import org.gradle.api.tasks.StopExecutionException
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Zip
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import java.io.File

private val defaultReleaseDir: File
	get() {
		val envVarName = "RELEASE_HOME"
		val releaseHome = checkNotNull(System.getenv(envVarName)) {
			"Please set ${envVarName} environment variable to an existing directory."
		}
		return File(releaseHome).also {
			check(it.exists() && it.isDirectory) {
				"Please set ${envVarName} environment variable to an existing directory."
			}
		}
	}

class AndroidReleasePlugin : BasePlugin() {

	lateinit var android: BaseExtension

	override fun apply(target: Project) {
		super.apply(target)

		android = project.extensions.getByName<BaseExtension>("android")

		val allTask = registerReleaseAllTask()
		project.afterEvaluate {
			android.buildTypes.forEach { buildType ->
				val releaseBuildTypeTask = registerReleaseTasks(buildType)
				allTask { dependsOn(releaseBuildTypeTask) }
			}
		}
	}

	private fun registerReleaseAllTask(): TaskProvider<Task> =
		project.tasks.register<Task>("release") {
			group = org.gradle.api.plugins.BasePlugin.UPLOAD_GROUP
			description = "Assembles and archives all builds"
		}

	private fun registerReleaseTasks(buildType: BuildType): TaskProvider<Task> {
		val allBuildTypeTask = project.tasks.register<Task>("releaseAll${buildType.name.capitalize()}") {
			group = org.gradle.api.plugins.BasePlugin.UPLOAD_GROUP
			description = "Assembles and archives all ${buildType.name} builds"
		}
		LOG.debug("Creating tasks for {}", buildType.name)
		fun registerReleaseTaskWithDependency(variant: ApkVariant) {
			val buildTypeTask = registerReleaseTask(variant)
			allBuildTypeTask { dependsOn(buildTypeTask) }
			variant.productFlavors.forEach { flavor ->
				val flavorTask = registerFlavorTask(flavor)
				flavorTask { dependsOn(buildTypeTask) }
			}
		}

		@Suppress("UNCHECKED_CAST")
		fun getVariantsForBuildType(): DomainObjectCollection<ApkVariant> =
			android.variants.matching { it.buildType.name == buildType.name } as DomainObjectSet<ApkVariant>

		project.plugins.withType<AppPlugin> {
			val matching = getVariantsForBuildType()
			LOG.debug("Found android app, variants with {}: {}", buildType.name, matching)
			matching.all(::registerReleaseTaskWithDependency)
		}
		project.plugins.withType<LibraryPlugin> {
			val matching = getVariantsForBuildType()
			LOG.debug("Found android lib, variants with {}: {}", buildType.name, matching)
			matching.all(::registerReleaseTaskWithDependency)
		}
		return allBuildTypeTask
	}

	private fun registerReleaseTask(variant: ApkVariant): TaskProvider<Zip> {
		val releaseVariantTask = project.tasks.register<Zip>("release${variant.name.capitalize()}") {
			group = org.gradle.api.plugins.BasePlugin.UPLOAD_GROUP
			description = "Assembles and archives apk and its ProGuard mapping for ${variant.description}"
			destinationDirectory.fileProvider(project.provider { defaultReleaseDir.resolve("android") })
			archiveFileName.set(
				android.defaultConfig
					.extensions.getByName<AndroidVersionExtension>(AndroidVersionExtension.NAME)
					.formatArtifactName(
						project,
						"archive",
						variant.applicationId,
						variant.versionCode.toLong(),
						variant.versionName
					) + ".zip"
			)
			from(variant.packageApplicationProvider.get().outputDirectory) {
				it.exclude(BuiltArtifactsImpl.METADATA_FILE_NAME)
			}
			if (variant is TestedVariant && variant.testVariant != null) {
				from(variant.testVariant.packageApplicationProvider.get().outputDirectory) {
					it.exclude(BuiltArtifactsImpl.METADATA_FILE_NAME)
				}
			}
			if (variant.buildType.isMinifyEnabled) {
				from(variant.mappingFileProvider.map { it.singleFile.parentFile }) {
					it.include("*")
					it.rename("(.*)", "proguard_$1")
				}
			}
			doFirst {
				val outFile = outputs.files.singleFile
				if (outFile.exists()) {
					throw StopExecutionException("Target zip file already exists, did you run 'svn update'?\nRelease archive: ${outFile}")
				}
			}
			doLast {
				println("Published release artifacts to ${outputs.files.singleFile}")
			}
		}
		releaseVariantTask { dependsOn(variant.assembleProvider) }
		if (variant is TestedVariant && variant.testVariant != null) {
			releaseVariantTask { dependsOn(variant.testVariant.assembleProvider) }
		}
		return releaseVariantTask
		/*val task: AndroidReleaseTask = variant.install.project.tasks.create(
			mapOf(
				"name" to "release${variant.name.capitalize()}",
				"type" to AndroidReleaserTask::class.java,
				"dependsOn" to variant.assemble
			)
		)
		task.variant = variant*/
	}

	private fun registerFlavorTask(flavor: ProductFlavor): TaskProvider<Task> {
		val releaseFlavorTaskName = "release${flavor.name.capitalize()}"
		// Get the flavor task in case it was already registered by another variant.
		var releaseFlavorTask = try {
			project.tasks.named(releaseFlavorTaskName)
		} catch (ex: UnknownTaskException) {
			null
		}
		if (releaseFlavorTask == null) {
			releaseFlavorTask = project.tasks.register<Task>(releaseFlavorTaskName) {
				group = org.gradle.api.plugins.BasePlugin.UPLOAD_GROUP
				description = "Assembles and archives all builds for flavor ${flavor.name}"
			}
		}
		return releaseFlavorTask
	}
}
