package net.twisterrob.gradle.android

import com.android.build.gradle.*
import com.android.build.gradle.api.*
import com.android.build.gradle.internal.variant.BaseVariantData
import com.android.builder.core.DefaultApiVersion
import net.twisterrob.gradle.common.BasePlugin
import org.gradle.api.Project
import org.gradle.api.internal.DefaultDomainObjectSet

import static com.android.builder.core.AndroidBuilder.*

class AndroidBuildPluginExtension {
	boolean decorateBuildConfig = true
	boolean addRunTasks = true
}

public class AndroidBuildPlugin extends BasePlugin {
	private BaseExtension android

	@Override
	void apply(Project target) {
		super.apply(target)
		android = project.android

		def twisterrob = android.extensions.create('twisterrob', AndroidBuildPluginExtension)

		android.with {
			lintOptions.with {
				xmlReport = false
				checkAllWarnings = true
				abortOnError = true
				disable 'Assert', 'GoogleAppIndexingWarning'
				fatal 'StopShip' // http://stackoverflow.com/q/33504186/253468
			}
			buildToolsVersion "23.0.2" // latest Android SDK Build-tools
			compileSdkVersion "android-23" // Android 6.0/SDK Platform

			defaultConfig.with {
				setMinSdkVersion(new DefaultApiVersion(10)) // 2.3.3 Gingerbread MR1
				setTargetSdkVersion(new DefaultApiVersion(19)) // 4.4 KitKat
				addBuildConfigField createClassField("String", "EMAIL", "\"feedback@twisterrob.net\"")
			}

			buildTypes['debug'].with {
				project.plugins.withType(AppPlugin) {
					// TODO make debug buildtypes configurable, use name of buildtype as suffix
					setApplicationIdSuffix(".debug")
				}
				setVersionNameSuffix("d")
				addBuildConfigField createClassField("String", "EMAIL", "\"papp.robert.s@gmail.com\"");
				addResValue createClassField("bool", "in_test", "true");
				addResValue createClassField("bool", "in_prod", "false");
			}
			buildTypes['release'].with {
				addResValue createClassField("bool", "in_test", "false")
				addResValue createClassField("bool", "in_prod", "true")
			}
		}

		project.afterEvaluate {
			project.plugins.withType(AppPlugin) {
				//android.applicationVariants.all this.&fixVariantTaskGroups

				if (twisterrob.decorateBuildConfig) {
					decorateBuildConfig()
					android.applicationVariants.all this.&addPackageName
				}

				if (twisterrob.addRunTasks) {
					android.applicationVariants.all this.&createRunTask
				}
			}

			project.plugins.withType(LibraryPlugin) {
				//android.libraryVariants.all this.&fixVariantTaskGroups
			}
		}
	}
	void decorateBuildConfig() {
		project.tasks.create('decorateBuildConfig', DecorateBuildConfigTask) {
			description 'Add more information about build to BuildConfig.java'
			project.tasks.preBuild.dependsOn delegate
		}
	}

	static void createRunTask(ApkVariant variant) {
		if (variant.install) {
			variant.install.project.tasks.create(
					name: "run${variant.name.capitalize()}",
					type: AndroidInstallRunnerTask,
					dependsOn: variant.install
			).variant = variant
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
