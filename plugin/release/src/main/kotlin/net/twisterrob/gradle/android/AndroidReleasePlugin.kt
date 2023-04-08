package net.twisterrob.gradle.android

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationVariant
import com.android.build.api.variant.Variant
import com.android.build.api.variant.impl.BuiltArtifactsImpl
import com.android.build.gradle.BaseExtension
import com.android.builder.model.BuildType
import net.twisterrob.gradle.common.BasePlugin
import net.twisterrob.gradle.internal.android.unwrapCast
import net.twisterrob.gradle.kotlin.dsl.extensions
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.Directory
import org.gradle.api.logging.Logger
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Zip
import org.gradle.kotlin.dsl.closureOf
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.register
import java.io.File
import java.io.IOException
import java.util.zip.ZipFile

class AndroidReleasePlugin : BasePlugin() {

	override fun apply(target: Project) {
		super.apply(target)
		val android = project.extensions.getByName<BaseExtension>("android")
		android.extensions.create<AndroidReleaseExtension>(AndroidReleaseExtension.NAME).apply {
			directory.convention(project.releaseDirectory())
		}

		val releaseEachTask = registerReleaseEachTask()
		android.buildTypes.configureEach { buildType ->
			val releaseBuildTypeTask = registerReleaseTasks(android, buildType)
			releaseEachTask.configure { it.dependsOn(releaseBuildTypeTask) }
		}
	}

	private fun registerReleaseEachTask(): TaskProvider<Task> =
		project.tasks.register<Task>("release") {
			group = org.gradle.api.publish.plugins.PublishingPlugin.PUBLISH_TASK_GROUP
			description = "Calls each release task for all build types"
		}

	private fun registerReleaseTasks(android: BaseExtension, buildType: BuildType): TaskProvider<Task> {
		val releaseBuildTypeTask = project.tasks.register<Task>("releaseAll${buildType.name.capitalize()}") {
			group = org.gradle.api.publish.plugins.PublishingPlugin.PUBLISH_TASK_GROUP
			description = "Assembles and archives all ${buildType.name} builds"
		}
		LOG.debug("Creating tasks for {}", buildType.name)

		val version = AndroidVersionExtension.from(android)
		val release = AndroidReleaseExtension.from(android)
		val withBuildType = project.androidComponents.selector().withBuildType(buildType.name)
		project.androidComponents.onVariants(withBuildType) { variant ->
			val appVariant = variant.unwrapCast<Variant, ApplicationVariant>()
			val releaseVariantTask = registerReleaseTask(version, release, appVariant)
			releaseBuildTypeTask.configure { it.dependsOn(releaseVariantTask) }
		}
		return releaseBuildTypeTask
	}

	private fun registerReleaseTask(
		version: AndroidVersionExtension,
		release: AndroidReleaseExtension,
		variant: ApplicationVariant
	): TaskProvider<Zip> =
		project.tasks.register<Zip>("release${variant.name.capitalize()}") {
			group = org.gradle.api.publish.plugins.PublishingPlugin.PUBLISH_TASK_GROUP
			description = "Assembles and archives apk and its ProGuard mapping for ${variant.name} build"
			destinationDirectory.convention(release.directory)
			val out = variant.outputs.single()
			inputs.property("variant-applicationId", variant.applicationId)
			inputs.property("variant-versionName", out.versionName)
			inputs.property("variant-versionCode", out.versionCode)

			archiveFileName.convention(project.provider {
				@Suppress("UNNECESSARY_NOT_NULL_ASSERTION", "UnsafeCallOnNullableType")
				val versionCode = out.versionCode.get()!!.toLong()
				val versionName = out.versionName.get()
				val applicationId = variant.applicationId.get()
				version.formatArtifactName(project, "archive", applicationId, versionCode, versionName) + ".zip"
			})

			from(variant.artifacts.get(SingleArtifact.APK)) { copy ->
				copy.exclude(BuiltArtifactsImpl.METADATA_FILE_NAME)
			}

			if (variant.isMinifyEnabledCompat) {
				val mappingFileProvider = variant.artifacts.get(SingleArtifact.OBFUSCATION_MAPPING_FILE)
				archiveMappingFile(mappingFileProvider.map { it.asFile })
			}

			variant.androidTestCompat?.let { androidTest ->
				from(androidTest.artifacts.get(SingleArtifact.APK)) { copy ->
					copy.exclude(BuiltArtifactsImpl.METADATA_FILE_NAME)
				}
			}

			doFirst(closureOf<Zip> { failIfAlreadyArchived() })
			doLast(closureOf<Zip> { printResultingArchive() })
		}

	companion object {
		private const val RELEASE_PROPERTY = "net.twisterrob.android.release.directory"
		private const val RELEASE_ENV = "RELEASE_HOME"

		private fun Project.releaseDirectory(): Provider<Directory> {
			val defaultReleaseDir = layout.buildDirectory.dir("release")
			val externalReleaseDir = providers.gradleProperty(RELEASE_PROPERTY)
				.orElse(providers.environmentVariable(RELEASE_ENV).deprecated(logger))
				.map(::file)
			return layout.dir(externalReleaseDir).orElse(defaultReleaseDir)
		}

		private fun <T : Any> Provider<T>.deprecated(logger: Logger): Provider<T> =
			this.map {
				logger.warn(
					"The environment variable ${RELEASE_ENV} is deprecated, use the Gradle property ${RELEASE_PROPERTY} instead."
				)
				return@map it
			}
	}
}

private fun Zip.archiveMappingFile(mappingFileProvider: Provider<File>) {
	from(mappingFileProvider.map { it.parentFile }) { copy ->
		copy.include("*")
		copy.rename("(.*)", "proguard_$1")
	}
}

private fun Zip.failIfAlreadyArchived() {
	val outFile = outputs.files.singleFile
	if (outFile.exists()) {
		throw IOException("Target zip file already exists.\nRelease archive: ${outFile}")
	}
}

private fun Zip.printResultingArchive() {
	val contents = ZipFile(outputs.files.singleFile).use { zip ->
		zip
			.entries()
			.toList()
			.sortedBy { it.name }
			.joinToString(prefix = "\n", separator = "\n") { "\t * ${it}" }
	}
	logger.quiet("Published release artifacts to ${outputs.files.singleFile}:" + contents)
}
