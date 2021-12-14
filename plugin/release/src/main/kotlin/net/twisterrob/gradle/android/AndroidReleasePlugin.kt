package net.twisterrob.gradle.android

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.impl.ApplicationVariantImpl
import com.android.build.api.variant.impl.BuiltArtifactsImpl
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.internal.api.TestedVariant
import com.android.builder.model.BuildType
import com.android.builder.model.ProductFlavor
import net.twisterrob.gradle.base.BasePlugin
import net.twisterrob.gradle.common.AGPVersions
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
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import java.io.File
import java.util.zip.ZipFile

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

		val releaseAllTask = registerReleaseAllTask()
		if (AGPVersions.CLASSPATH > AGPVersions.v70x) {
			android.buildTypes.forEach { buildType ->
				val releaseBuildTypeTask = registerReleaseTasks(buildType)
				releaseAllTask.configure { it.dependsOn(releaseBuildTypeTask) }
			}
		} else {
			project.afterEvaluate {
				android.buildTypes.forEach { buildType ->
					val releaseBuildTypeTask = registerReleaseTasks(buildType)
					releaseAllTask.configure { it.dependsOn(releaseBuildTypeTask) }
				}
			}
		}
	}

	private fun registerReleaseAllTask(): TaskProvider<Task> =
		project.tasks.register<Task>("release") {
			group = org.gradle.api.publish.plugins.PublishingPlugin.PUBLISH_TASK_GROUP
			description = "Assembles and archives all builds"
		}

	private fun registerReleaseTasks(buildType: BuildType): TaskProvider<Task> {
		val releaseBuildTypeTask = project.tasks.register<Task>("releaseAll${buildType.name.capitalize()}") {
			group = org.gradle.api.publish.plugins.PublishingPlugin.PUBLISH_TASK_GROUP
			description = "Assembles and archives all ${buildType.name} builds"
		}
		LOG.debug("Creating tasks for {}", buildType.name)

		if (AGPVersions.CLASSPATH > AGPVersions.v70x) {
			val withBuildType = project.androidComponents.selector().withBuildType(buildType.name)
			project.androidComponents.onVariants(withBuildType) { variant ->
				val releaseVariantTask = registerReleaseTask(variant as ApplicationVariantImpl)
				releaseBuildTypeTask.configure { it.dependsOn(releaseVariantTask) }
			}
		} else {
			@Suppress("UNCHECKED_CAST")
			val variantsForBuildType: DomainObjectCollection<ApkVariant> =
				android.variants.matching { it.buildType.name == buildType.name } as DomainObjectSet<ApkVariant>
			variantsForBuildType.all { variant ->
				val releaseVariantTask = registerReleaseTask(variant)
				releaseBuildTypeTask.configure { it.dependsOn(releaseVariantTask) }
				variant.productFlavors.forEach { flavor ->
					val releaseFlavorTask = registerFlavorTask(flavor)
					releaseFlavorTask.configure { it.dependsOn(releaseVariantTask) }
				}
			}
		}

		return releaseBuildTypeTask
	}

	private fun registerReleaseTask(variant: ApkVariant): TaskProvider<Zip> =
		project.tasks.register<Zip>("release${variant.name.capitalize()}") {
			group = org.gradle.api.publish.plugins.PublishingPlugin.PUBLISH_TASK_GROUP
			description = "Assembles and archives apk and its ProGuard mapping for ${variant.description}"
			destinationDirectory.fileProvider(project.provider { defaultReleaseDir.resolve("android") })
			val archiveFormat =
				android.defaultConfig.extensions.getByType<AndroidVersionExtension>().formatArtifactName
			val releaseZipFileName = with(variant) {
				archiveFormat(project, "archive", applicationId, versionCode.toLong(), versionName) + ".zip"
			}
			archiveFileName.set(releaseZipFileName)

			fun useOutput(variant: ApkVariant) {
				dependsOn(variant.assembleProvider)
				from(variant.packageApplicationProvider.get().outputDirectory) {
					it.exclude(BuiltArtifactsImpl.METADATA_FILE_NAME)
				}
			}

			useOutput(variant)
			if (variant is TestedVariant) {
				variant.testVariant?.let(::useOutput)
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
				println("Published release artifacts to ${outputs.files.singleFile}:" + ZipFile(outputs.files.singleFile)
					.entries()
					.toList()
					.sortedBy { it.name }
					.joinToString(prefix = "\n", separator = "\n") { "\t * ${it}" }
				)
			}
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
				group = org.gradle.api.publish.plugins.PublishingPlugin.PUBLISH_TASK_GROUP
				description = "Assembles and archives all builds for flavor ${flavor.name}"
			}
		}
		return releaseFlavorTask
	}

	private fun registerReleaseTask(variant: ApplicationVariantImpl): TaskProvider<Zip> =
		project.tasks.register<Zip>("release${variant.name.capitalize()}") {
			group = org.gradle.api.publish.plugins.PublishingPlugin.PUBLISH_TASK_GROUP
			description = "Assembles and archives apk and its ProGuard mapping for ${variant.name} build"
			destinationDirectory.fileProvider(project.provider { defaultReleaseDir.resolve("android") })
			val archiveFormat =
				android.defaultConfig.extensions.getByType<AndroidVersionExtension>().formatArtifactName
			val out = variant.outputs.single()
			inputs.property("variant-applicationId", variant.applicationId)
			inputs.property("variant-versionName", out.versionName)
			inputs.property("variant-versionCode", out.versionCode)

			archiveFileName.set(project.provider {
				@Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
				val versionCode = out.versionCode.get()!!.toLong()
				val versionName = out.versionName.get()
				archiveFormat(project, "archive", variant.applicationId.get(), versionCode, versionName) + ".zip"
			})

			from(variant.artifacts.get(SingleArtifact.APK)) { copy ->
				copy.exclude(BuiltArtifactsImpl.METADATA_FILE_NAME)
			}

			if (variant.minifiedEnabled) {
				val mappingFileProvider = variant.artifacts.get(SingleArtifact.OBFUSCATION_MAPPING_FILE)
				from(mappingFileProvider.map { it.asFile.parentFile }) { copy ->
					copy.include("*")
					copy.rename("(.*)", "proguard_$1")
				}
			}

			variant.androidTest?.let { androidTest ->
				from( androidTest.artifacts.get(SingleArtifact.APK)) { copy ->
					copy.exclude(BuiltArtifactsImpl.METADATA_FILE_NAME)
				}
			}

			doFirst {
				val outFile = outputs.files.singleFile
				if (outFile.exists()) {
					throw StopExecutionException("Target zip file already exists.\nRelease archive: ${outFile}")
				}
			}

			doLast {
				println("Published release artifacts to ${outputs.files.singleFile}:" + ZipFile(outputs.files.singleFile)
					.entries()
					.toList()
					.sortedBy { it.name }
					.joinToString(prefix = "\n", separator = "\n") { "\t * ${it}" }
				)
			}
		}
}
