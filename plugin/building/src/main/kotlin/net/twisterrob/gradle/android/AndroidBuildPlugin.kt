package net.twisterrob.gradle.android

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.api.androidTestVariantData
import com.android.build.gradle.internal.api.unitTestVariantData
import com.android.build.gradle.internal.api.variantData
import com.android.build.gradle.internal.variant.BaseVariantData
import net.twisterrob.gradle.android.tasks.AndroidInstallRunnerTask
import net.twisterrob.gradle.android.tasks.CalculateBuildTimeTask
import net.twisterrob.gradle.android.tasks.CalculateBuildTimeTask.Companion.addBuildConfigFields
import net.twisterrob.gradle.android.tasks.CalculateVCSRevisionInfoTask
import net.twisterrob.gradle.android.tasks.CalculateVCSRevisionInfoTask.Companion.addBuildConfigFields
import net.twisterrob.gradle.base.BasePlugin
import net.twisterrob.gradle.common.AGPVersions
import net.twisterrob.gradle.kotlin.dsl.extensions
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

open class AndroidBuildPluginExtension {

	companion object {

		internal const val NAME: String = "twisterrob"
	}

	var decorateBuildConfig: Boolean = true
	var addRunTasks: Boolean = true
}

class AndroidBuildPlugin : BasePlugin() {

	private lateinit var android: BaseExtension

	override fun apply(target: Project) {
		super.apply(target)
		android = project.extensions.getByName<BaseExtension>("android")

		val twisterrob = android.extensions.create<AndroidBuildPluginExtension>(AndroidBuildPluginExtension.NAME)

		// :lintVitalRelease trying to resolve :lintClassPath that has Groovy, Kotlin and some libs
		project.repositories.mavenCentral() // https://repo.maven.apache.org/maven2/
		// most of Android's stuff is distributed here, so add by default
		project.repositories.google() // https://maven.google.com

		with(android) {
			with(lintOptions) {
				xmlReport = false
				isCheckAllWarnings = true
				isAbortOnError = true
				disable("Assert", "GoogleAppIndexingWarning")
				fatal("StopShip") // http://stackoverflow.com/q/33504186/253468
			}
			// TODO intentionally mismatching the versions to get latest features, but still have sources available for compiled version.
			buildToolsVersion = VERSION_BUILD_TOOLS
			compileSdkVersion = "android-${VERSION_SDK_COMPILE}"

			with(defaultConfig) {
				minSdkVersion(VERSION_SDK_MINIMUM)
				targetSdkVersion(VERSION_SDK_TARGET)
				vectorDrawables.useSupportLibrary = true
			}

			buildTypes.configure("debug") { debug ->
				project.plugins.withType<AppPlugin> {
					// TODO make debug buildTypes configurable, use name of buildType as suffix
					debug.setApplicationIdSuffix(".${debug.name}")
				}
				debug.setVersionNameSuffix("d")
				debug.resValue("bool", "in_test", "true")
				debug.resValue("bool", "in_prod", "false")
			}

			buildTypes.configure("release") { release ->
				release.resValue("bool", "in_test", "false")
				release.resValue("bool", "in_prod", "true")
			}

			// configure files we don't need in APKs
			with(packagingOptions) {
				// support-annotations-28.0.0.jar contains this file
				// it's for Android Gradle Plugin at best, if at all used
				exclude("META-INF/proguard/androidx-annotations.pro")

				// Each Android Support Library component has a separate entry for storing version.
				// Probably used by Google Play to do statistics, gracefully opt out of this.
				exclude("META-INF/android.*.version")
				exclude("META-INF/androidx.*.version")

				// Kotlin builds these things in, found no docs so far about their necessity, so try to exclude
				exclude("**/*.kotlin_metadata")
				exclude("**/*.kotlin_module")
				exclude("**/*.kotlin_builtins")

				// Readmes
				// (e.g. hamcrest-library-2.1.jar and hamcrest-core-2.1.jar both pack a readme to encourage upgrade)
				exclude("**/README.txt")
				exclude("**/README.md")
			}
		}

		val buildTimeTaskProvider =
			project.tasks.register<CalculateBuildTimeTask>("calculateBuildConfigBuildTime")
		val vcsTaskProvider =
			project.tasks.register<CalculateVCSRevisionInfoTask>("calculateBuildConfigVCSRevisionInfo")

		when {
			AGPVersions.CLASSPATH >= AGPVersions.v70x -> {
				project.androidComponents.finalizeDsl {
					if (twisterrob.decorateBuildConfig && android.buildFeatures.buildConfig != false) {
						decorateBuildConfig(buildTimeTaskProvider, vcsTaskProvider)
					}
				}
			}
			else -> {
				project.beforeAndroidTasksCreated {
					if (twisterrob.decorateBuildConfig && android.buildFeatures.buildConfig != false) {
						decorateBuildConfig(buildTimeTaskProvider, vcsTaskProvider)
					}
				}
			}
		}
		project.plugins.withType<AppPlugin> {
			if (twisterrob.decorateBuildConfig) {
				android.variants.all(::addPackageName)
			}
		}
		project.afterEvaluate {
			if (project.plugins.hasAndroid()) {
				android.variants.all(::fixVariantTaskGroups)
			}
			project.plugins.withType<AppPlugin> {
				if (twisterrob.addRunTasks) {
					android.variants.all { variant -> createRunTask(variant as ApkVariant) }
				}
			}
		}
	}

	/**
	 * This is a new incubating way of adding buildConfigFields introduced in AGP 4.1.
	 * @see https://issuetracker.google.com/issues/172657565
	 * @see https://github.com/android/gradle-recipes/blob/8d0c14d6fed86726df60fb8c8f79e5a03c66fdee/Kotlin/addCustomFieldWithValueFromTask/app/build.gradle.kts
	 */
	private fun decorateBuildConfig(
		buildTimeTaskProvider: TaskProvider<CalculateBuildTimeTask>,
		vcsTaskProvider: TaskProvider<CalculateVCSRevisionInfoTask>
	) {
		buildTimeTaskProvider.addBuildConfigFields(project)
		vcsTaskProvider.addBuildConfigFields(project)
	}

	companion object {

		private fun createRunTask(variant: ApkVariant) {
			val install = variant.installProvider?.get()
			if (install != null) {
				val project = install.project
				val name = "run${variant.name.capitalize()}"
				project.tasks.create<AndroidInstallRunnerTask>(name) {
					dependsOn(install)
					this.setVariant(variant)
				}
			}
		}

		private fun fixVariantTaskGroups(variant: BaseVariant) {
			fun BaseVariantData.fixTaskMetadata() {
				taskContainerCompat.compileTask.configure { task ->
					task.group = "Build"
					task.description = "Compiles sources for ${description}."
				}
				taskContainerCompat.javacTask.configure { task ->
					task.group = "Build"
					task.description = "Compiles Java sources for ${description}."
				}
			}
			variant.variantData?.fixTaskMetadata()
			variant.androidTestVariantData?.fixTaskMetadata()
			variant.unitTestVariantData?.fixTaskMetadata()
		}

		private fun addPackageName(variant: BaseVariant) {
			// Package name for use e.g. in preferences to launch intent from the right package or for content providers
			variant.resValue("string", "app_package", variant.applicationId)
		}
	}
}
