package net.twisterrob.gradle.android

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.api.androidTestVariantData
import com.android.build.gradle.internal.api.unitTestVariantData
import com.android.build.gradle.internal.api.variantData
import com.android.build.gradle.internal.variant.BaseVariantData
import com.android.builder.core.DefaultApiVersion
import net.twisterrob.gradle.android.tasks.AndroidInstallRunnerTask
import net.twisterrob.gradle.android.tasks.DecorateBuildConfigTask
import net.twisterrob.gradle.base.BasePlugin
import net.twisterrob.gradle.kotlin.dsl.extensions
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.withType

open class AndroidBuildPluginExtension {

	var decorateBuildConfig: Boolean = true
	var addRunTasks: Boolean = true
}

/**
 * Keep it in sync with AppCompat's minimum.
 */
const val VERSION_SDK_MINIMUM = 14

/**
 * Latest SDK version available, Google Play Store has stringent rules, so keep up to date.
 */
const val VERSION_SDK_TARGET = 28

/**
 * Latest SDK version available, useful for discovering deprecated methods and getting new features like `.findViewById<T>()`.
 */
const val VERSION_SDK_COMPILE = 28
/**
 * Note: format changed at 9 Pie, was 8.1.0 Oreo.
 */
const val VERSION_SDK_COMPILE_NAME = "9" // Pie

/**
 * Latest build tools version available, there's no reason to hold back.
 */
const val VERSION_BUILD_TOOLS = "28.0.3"

class AndroidBuildPlugin : BasePlugin() {

	private lateinit var android: BaseExtension

	override fun apply(target: Project) {
		super.apply(target)
		android = project.extensions.getByName<BaseExtension>("android")

		val twisterrob = android.extensions.create("twisterrob", AndroidBuildPluginExtension::class.java)

		// :lintVitalRelease trying to resolve :lintClassPath that has Groovy, Kotlin and some libs
		// otherwise default maven repo that's a superset of mavenCentral(), so prefer this
		project.repositories.jcenter() // http://jcenter.bintray.com/
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
				minSdkVersion = DefaultApiVersion(VERSION_SDK_MINIMUM)
				targetSdkVersion = DefaultApiVersion(VERSION_SDK_TARGET)
				vectorDrawables.useSupportLibrary = true
				buildConfigField("String", "EMAIL", "\"feedback@twisterrob.net\"")
			}

			with(buildTypes["debug"]) debug@{
				project.plugins.withType<AppPlugin> {
					// TODO make debug buildTypes configurable, use name of buildType as suffix
					this@debug.applicationIdSuffix = ".${this@debug.name}"
				}
				versionNameSuffix = "d"
				buildConfigField("String", "EMAIL", "\"papp.robert.s@gmail.com\"")
				resValue("bool", "in_test", "true")
				resValue("bool", "in_prod", "false")
			}
			with(buildTypes["release"]) release@{
				resValue("bool", "in_test", "false")
				resValue("bool", "in_prod", "true")
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
			}
		}

		if (twisterrob.decorateBuildConfig) {
			decorateBuildConfig()
		}
		project.afterEvaluate {
			if (project.plugins.hasAndroid()) {
				android.variants.all(::fixVariantTaskGroups)
			}
			project.plugins.withType<AppPlugin> {
				if (twisterrob.decorateBuildConfig) {
					android.variants.all(::addPackageName)
				}

				if (twisterrob.addRunTasks) {
					android.variants.all { variant -> createRunTask(variant as ApkVariant) }
				}
			}
		}
	}

	private fun decorateBuildConfig() {
		project.tasks.create<DecorateBuildConfigTask>("decorateBuildConfig") {
			description = "Adds more information about build to BuildConfig.java."
			project.tasks["preBuild"].dependsOn(this)
		}
	}

	companion object {

		private fun createRunTask(variant: ApkVariant) {
			if (variant.install != null) {
				val project = variant.install.project
				val name = "run${variant.name.capitalize()}"
				project.tasks.create<AndroidInstallRunnerTask>(name) {
					dependsOn(variant.install)
					this.setVariant(variant)
				}
			}
		}

		private fun fixVariantTaskGroups(variant: BaseVariant) {
			fun BaseVariantData.fixTaskMetadata() {
				compileTask.group = "Build"
				compileTask.description = "Compiles sources for ${description}."
				javacTask.group = "Build"
				javacTask.description = "Compiles Java sources for ${description}."
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
