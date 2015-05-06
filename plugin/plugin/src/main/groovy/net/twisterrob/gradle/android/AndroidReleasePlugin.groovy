package net.twisterrob.gradle.android

import com.android.build.gradle.*
import com.android.build.gradle.api.ApkVariant
import com.android.builder.model.BuildType
import net.twisterrob.gradle.common.BasePlugin
import org.gradle.api.*
import org.gradle.api.tasks.bundling.Zip

public class AndroidReleasePlugin extends BasePlugin {
	BaseExtension android

	@Override
	void apply(Project target) {
		super.apply(target)

		android = project.android

		project.afterEvaluate {
			createReleaseTasks(android.buildTypes['release'])
		}
	}

	private void createReleaseTasks(BuildType buildType) {
		LOG.debug("Creating tasks for {}", buildType.name)
		def allTask = project.tasks.create(name: 'release') {
			group org.gradle.api.plugins.BasePlugin.BUILD_GROUP
			description "Assembles and archives all ${buildType.name} builds"
		}
		def createReleaseTask = this.&createReleaseTask.curry(allTask)
		project.plugins.withType(AppPlugin) {
			def matching = android.applicationVariants.matching { it.buildType.name == buildType.name }
			LOG.debug("Found android app, variants with {}: {}", buildType.name, matching)
			matching.all createReleaseTask
		}
		project.plugins.withType(LibraryPlugin) {
			def matching = android.libraryVariants.matching { it.buildType.name == buildType.name }
			LOG.debug("Found android lib, variants with {}: {}", buildType.name, matching)
			matching.all createReleaseTask
		}
	}

	void createReleaseTask(Task releaseAllTask, ApkVariant variant) {
		def releaseVariantTask = project.tasks.create(name: "release${variant.name.capitalize()}", type: Zip) {
			group org.gradle.api.plugins.BasePlugin.BUILD_GROUP
			description "Assembles and archives apk and its proguard mapping for ${variant.description}"
			String releaseDir = System.getenv('RELEASE_HOME')
			if (releaseDir == null) {
				throw new IllegalArgumentException("Please set RELEASE_HOME environment variable to a directory.")
			}
			destinationDir new File(releaseDir, "android")
			archiveName android.defaultConfig.version.formatArtifactName(project, variant, "archive") + ".zip"
			variant.outputs.each { output ->
				from(output.outputFile)
			}
			if (variant.buildType.minifyEnabled) {
				from(variant.mappingFile.getParentFile()) {
					include '*'
					rename '(.*)', 'proguard_$1'
				}
			}
		}
		releaseVariantTask.dependsOn variant.assemble
		releaseAllTask.dependsOn releaseVariantTask

		variant.productFlavors.each { flavor ->
			def releaseFlavorTaskName = "release${flavor.name.capitalize()}"
			def releaseFlavorTask = project.tasks.findByName(releaseFlavorTaskName)
			if (!releaseFlavorTask) {
				releaseFlavorTask = project.tasks.create(name: releaseFlavorTaskName) {
					group org.gradle.api.plugins.BasePlugin.BUILD_GROUP
					description "Assembles and archives all builds for flavor ${flavor.name}"
				}
			}
			releaseFlavorTask.dependsOn releaseVariantTask
		}
		/*variant.install.project.tasks.create(
				name: "release${variant.name.capitalize()}",
				type: AndroidReleaserTask,
				dependsOn: variant.assemble
		).variant = variant*/
	}
}
