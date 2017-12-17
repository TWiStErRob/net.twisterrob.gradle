package net.twisterrob.gradle.android

import com.android.build.gradle.*
import com.android.build.gradle.api.*
import com.android.build.gradle.internal.dsl.BuildType
import com.android.build.gradle.internal.variant.BaseVariantData
import com.android.builder.core.DefaultApiVersion
import net.twisterrob.gradle.Utils
import net.twisterrob.gradle.android.tasks.*
import net.twisterrob.gradle.common.BasePlugin
import org.gradle.api.Project
import org.gradle.api.internal.DefaultDomainObjectSet

class AndroidBuildPluginExtension {
	boolean decorateBuildConfig = true
	boolean addRunTasks = true
}

class AndroidBuildPlugin extends BasePlugin {
	private BaseExtension android

	@Override
	void apply(Project target) {
		super.apply(target)
		android = project.android

		def twisterrob = android.extensions.create('twisterrob', AndroidBuildPluginExtension)

		project.repositories.google() // https://maven.google.com

		android.with {
			lintOptions.with {
				xmlReport = false
				checkAllWarnings = true
				abortOnError = true
				disable 'Assert', 'GoogleAppIndexingWarning'
				fatal 'StopShip' // http://stackoverflow.com/q/33504186/253468
			}
			// TODO intentionally mismatching the versions to get latest features, but still have sources available for compiled version.
			buildToolsVersion "27.0.2" // latest Android SDK Build-tools
			compileSdkVersion "android-26" // Android 7.0/SDK Platform

			defaultConfig.with {
				setMinSdkVersion(new DefaultApiVersion(10)) // 2.3.3 Gingerbread MR1
				setTargetSdkVersion(new DefaultApiVersion(19)) // 4.4 KitKat
				vectorDrawables.useSupportLibrary = true
				buildConfigField "String", "EMAIL", "\"feedback@twisterrob.net\""
			}

			buildTypes['debug'].with { BuildType buildType ->
				project.plugins.withType(AppPlugin) {
					// TODO make debug buildTypes configurable, use name of buildType as suffix
					buildType.setApplicationIdSuffix(".${buildType.name}")
				}
				buildType.setVersionNameSuffix("d")
				buildType.buildConfigField "String", "EMAIL", "\"papp.robert.s@gmail.com\""
				buildType.resValue "bool", "in_test", "true"
				buildType.resValue "bool", "in_prod", "false"
			}
			buildTypes['release'].with { BuildType buildType ->
				buildType.resValue "bool", "in_test", "false"
				buildType.resValue "bool", "in_prod", "true"
			}
		}

		if (twisterrob.decorateBuildConfig) {
			decorateBuildConfig()
		}
		project.afterEvaluate {
			project.plugins.withType(AppPlugin) {
				//Utils.getVariants(android).all this.&fixVariantTaskGroups

				if (twisterrob.decorateBuildConfig) {
					Utils.getVariants(android).all this.&addPackageName
				}

				if (twisterrob.addRunTasks) {
					Utils.getVariants(android).all this.&createRunTask
				}
			}

			project.plugins.withType(LibraryPlugin) {
				//Utils.getVariants(android).all this.&fixVariantTaskGroups
			}
		}
	}
	void decorateBuildConfig() {
		project.tasks.create('decorateBuildConfig', DecorateBuildConfigTask) {
			description 'Add more information about build to BuildConfig.java'
			project.tasks.preBuild.dependsOn delegate
		}
	}

	static void createRunTask(ApplicationVariant variant) {
		if (variant.install) {
			AndroidInstallRunnerTask task = (AndroidInstallRunnerTask)variant.install.project.tasks.create(
					name: "run${variant.name.capitalize()}",
					type: AndroidInstallRunnerTask,
					dependsOn: variant.install
			)
			task.variant = variant
		}
	}

	static void fixVariantTaskGroups(BaseVariant variant) {
		BaseVariantData variantData = variant.variantData
		variantData.compileTask.group = "Build"
		variantData.compileTask.description = "Compiles sources for ${variant.description}"
		BaseVariantData testVariantData = variantData.testVariantData
		if (testVariantData) {
			testVariantData.compileTask.group = "Build"
			testVariantData.compileTask.description = "Compiles test sources for ${variant.description}"
		}
	}

	DefaultDomainObjectSet<? extends BaseVariant> getVariants() {
		if (android instanceof LibraryExtension) {
			return android.libraryVariants
		} else if (android instanceof AppExtension) {
			return android.applicationVariants
		} else {
			throw new IllegalArgumentException("Cannot find variants on " + android)
		}
	}

	static void addPackageName(BaseVariant variant) {
		// Package name for use e.g. in preferences to launch intent from the right package or for content providers
		variant.resValue "string", "app_package", variant.applicationId
	}
}
