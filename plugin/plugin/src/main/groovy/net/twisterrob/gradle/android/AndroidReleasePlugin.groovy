package net.twisterrob.gradle.android

import com.android.build.gradle.*
import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.internal.api.TestedVariant
import com.android.builder.model.BuildType
import net.twisterrob.gradle.Utils
import net.twisterrob.gradle.base.BasePlugin
import org.gradle.api.*
import org.gradle.api.tasks.StopExecutionException
import org.gradle.api.tasks.bundling.Zip

class AndroidReleasePlugin extends BasePlugin {
	BaseExtension android

	@Override
	void apply(Project target) {
		super.apply(target)

		android = project.android

		def allTask = project.tasks.create(name: 'release') {
			group org.gradle.api.plugins.BasePlugin.UPLOAD_GROUP
			description "Assembles and archives all builds"
		}
		project.afterEvaluate {
			android.buildTypes.each {
				allTask.dependsOn createReleaseTasks(it)
			}
		}
	}

	private Task createReleaseTasks(BuildType buildType) {
		def allBuildTypeTask = project.tasks.create(name: "releaseAll${buildType.name.capitalize()}") {
			group org.gradle.api.plugins.BasePlugin.UPLOAD_GROUP
			description "Assembles and archives all ${buildType.name} builds"
		}
		LOG.debug("Creating tasks for {}", buildType.name)
		def createReleaseTask = { ApkVariant variant ->
			allBuildTypeTask.dependsOn createReleaseTask(variant)
		}
		project.plugins.withType(AppPlugin) {
			def matching = Utils.getVariants(android).matching { it.buildType.name == buildType.name }
			LOG.debug("Found android app, variants with {}: {}", buildType.name, matching)
			matching.all createReleaseTask
		}
		project.plugins.withType(LibraryPlugin) {
			def matching = Utils.getVariants(android).matching { it.buildType.name == buildType.name }
			LOG.debug("Found android lib, variants with {}: {}", buildType.name, matching)
			matching.all createReleaseTask
		}
		return allBuildTypeTask
	}

	Task createReleaseTask(ApkVariant variant) {
		def releaseVariantTask = project.tasks.create(name: "release${variant.name.capitalize()}", type: Zip) {
			group org.gradle.api.plugins.BasePlugin.UPLOAD_GROUP
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
			if (variant instanceof TestedVariant && variant.testVariant) {
				((TestedVariant)variant).testVariant.outputs.each { output ->
					from(output.outputFile)
				}
			}
			if (variant.buildType.minifyEnabled) {
				from(variant.mappingFile.getParentFile()) {
					include '*'
					rename '(.*)', 'proguard_$1'
				}
			}
			File outFile = outputs.files.singleFile
			doFirst {
				if (outFile.exists()) {
					throw new StopExecutionException(String.format(Locale.ROOT,
							"Target zip file already exists, did you run 'svn update'?\nRelease archive: %s",
							outFile))
				}
			}
			doLast {
				println "Published release artifacts to ${outFile}"
			}
		}
		releaseVariantTask.dependsOn variant.assemble
		if (variant instanceof TestedVariant && variant.testVariant) {
			releaseVariantTask.dependsOn variant.testVariant.assemble
		}

		variant.productFlavors.each { flavor ->
			def releaseFlavorTaskName = "release${flavor.name.capitalize()}"
			def releaseFlavorTask = project.tasks.findByName(releaseFlavorTaskName)
			if (!releaseFlavorTask) {
				releaseFlavorTask = project.tasks.create(name: releaseFlavorTaskName) {
					group org.gradle.api.plugins.BasePlugin.UPLOAD_GROUP
					description "Assembles and archives all builds for flavor ${flavor.name}"
				}
			}
			releaseFlavorTask.dependsOn releaseVariantTask
		}
		return releaseVariantTask
		/*variant.install.project.tasks.create(
				name: "release${variant.name.capitalize()}",
				type: AndroidReleaserTask,
				dependsOn: variant.assemble
		).variant = variant*/
	}
}
