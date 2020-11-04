package net.twisterrob.gradle.android

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.internal.api.TestedVariant
import com.android.builder.model.BuildType
import net.twisterrob.gradle.base.BasePlugin
import net.twisterrob.gradle.kotlin.dsl.extensions
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.StopExecutionException
import org.gradle.api.tasks.bundling.Zip
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.withType
import java.io.File

private const val envVarName = "RELEASE_HOME"

class AndroidReleasePlugin : BasePlugin() {

	lateinit var android: BaseExtension

	override fun apply(target: Project) {
		super.apply(target)

		android = project.extensions.getByName<BaseExtension>("android")

		val allTask = project.tasks.create<Task>("release") {
			group = org.gradle.api.plugins.BasePlugin.UPLOAD_GROUP
			description = "Assembles and archives all builds"
		}
		project.afterEvaluate {
			android.buildTypes.forEach { buildType ->
				allTask.dependsOn(createReleaseTasks(buildType))
			}
		}
	}

	private fun createReleaseTasks(buildType: BuildType): Task {
		val allBuildTypeTask = project.tasks.create<Task>("releaseAll${buildType.name.capitalize()}") {
			group = org.gradle.api.plugins.BasePlugin.UPLOAD_GROUP
			description = "Assembles and archives all ${buildType.name} builds"
		}
		LOG.debug("Creating tasks for {}", buildType.name)
		fun createReleaseTaskWithDependency(variant: ApkVariant) {
			allBuildTypeTask.dependsOn(createReleaseTask(variant))
		}

		@Suppress("UNCHECKED_CAST")
		fun getVariantsForBuildType() =
			android.variants.matching { it.buildType.name == buildType.name } as DomainObjectSet<ApkVariant>

		project.plugins.withType<AppPlugin> {
			val matching = getVariantsForBuildType()
			LOG.debug("Found android app, variants with {}: {}", buildType.name, matching)
			matching.all(::createReleaseTaskWithDependency)
		}
		project.plugins.withType<LibraryPlugin> {
			val matching = getVariantsForBuildType()
			LOG.debug("Found android lib, variants with {}: {}", buildType.name, matching)
			matching.all(::createReleaseTaskWithDependency)
		}
		return allBuildTypeTask
	}

	private fun createReleaseTask(variant: ApkVariant): Task {
		val releaseVariantTask = project.tasks.create<Zip>("release${variant.name.capitalize()}") {
			group = org.gradle.api.plugins.BasePlugin.UPLOAD_GROUP
			description = "Assembles and archives apk and its proguard mapping for ${variant.description}"
			val releaseDir = File(
				System.getenv(envVarName)
					?: throw IllegalArgumentException("Please set ${envVarName} environment variable to a directory.")
			)
			destinationDirectory.set(releaseDir.resolve("android"))
			archiveFileName.set(android.defaultConfig
				.extensions.getByName<AndroidVersionExtension>(AndroidVersionExtension.NAME)
				.formatArtifactName(project, variant, "archive") + ".zip")
			from(variant.packageApplicationProvider.get().outputDirectory) {
				it.exclude("output.json")
			}
			if (variant is TestedVariant && variant.testVariant != null) {
				from(variant.testVariant.packageApplicationProvider.get().outputDirectory) {
					it.exclude("output.json")
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
		releaseVariantTask.dependsOn(variant.assembleProvider)
		if (variant is TestedVariant && variant.testVariant != null) {
			releaseVariantTask.dependsOn(variant.testVariant.assembleProvider)
		}

		variant.productFlavors.forEach { flavor ->
			val releaseFlavorTaskName = "release${flavor.name.capitalize()}"
			var releaseFlavorTask = project.tasks.findByName(releaseFlavorTaskName)
			if (releaseFlavorTask == null) {
				releaseFlavorTask = project.tasks.create<Task>(releaseFlavorTaskName) {
					group = org.gradle.api.plugins.BasePlugin.UPLOAD_GROUP
					description = "Assembles and archives all builds for flavor ${flavor.name}"
				}
			}
			releaseFlavorTask.dependsOn(releaseVariantTask)
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
}
