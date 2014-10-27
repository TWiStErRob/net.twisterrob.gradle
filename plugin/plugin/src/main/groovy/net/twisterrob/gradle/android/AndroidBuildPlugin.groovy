package net.twisterrob.gradle.android

import com.android.build.gradle.*
import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.variant.BaseVariantData
import com.android.builder.core.DefaultApiVersion
import net.twisterrob.gradle.common.BasePlugin
import org.gradle.api.Project
import org.gradle.api.internal.DefaultDomainObjectSet

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
				disable 'Assert'
			}
			buildToolsVersion "21.0.2" // latest Android SDK Build-tools
			compileSdkVersion "android-21" // Android 5.0/SDK Platform

			defaultConfig.with {
				setMinSdkVersion(new DefaultApiVersion(10)) // 2.3.3 Gingerbread MR1
				setTargetSdkVersion(new DefaultApiVersion(19)) // 4.4 KitKat
				buildConfigField "String", "EMAIL", "\"feedback@twisterrob.net\""
			}

			buildTypes['debug'].with {
				// TODO make debug buildtypes configurable, use name of buildtype as suffix
				setApplicationIdSuffix(".debug")
				setVersionNameSuffix("d")
				buildConfigField "String", "EMAIL", "\"papp.robert.s@gmail.com\""
				resValue "bool", "in_test", "true"
				resValue "bool", "in_production", "false"
			}
			buildTypes['release'].with {
				resValue "bool", "in_test", "false"
				resValue "bool", "in_production", "true"
			}
		}

		project.afterEvaluate {
			project.plugins.withType(AppPlugin) {
				android.applicationVariants.all this.&fixVariantTaskGroups

				if (twisterrob.decorateBuildConfig) {
					decorateBuildConfig()
					android.applicationVariants.all this.&addPackageName
				}

				if (twisterrob.addRunTasks) {
					android.applicationVariants.all this.&createRunTask
				}
			}

			project.plugins.withType(LibraryPlugin) {
				android.libraryVariants.all this.&fixVariantTaskGroups
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

	static void addPackageName(ApplicationVariant variant) {
		// Need to have at least one resValue to NOT SKIP generateResValuesTask (i.e. run its doFirst)
		// Since buildType is attached to multiple flavors this value will be overwritten a few times
		// until the doFirst sets the final value, JUST BEFORE use
		variant.buildType.resValue "string", "app_package", "PLACEHOLDER for ${variant.name}"
		// The below should be variant.(add)resValue or variant.mergedFlavor.(add)resValue, but there's not such API,
		// overwriting before every use is a workaround, and it seems to work if there are flavors as well
		variant.variantData.generateResValuesTask.doFirst {
			// Add package for use e.g. in preferences to launch intent from the right package
			variant.buildType.resValue "string", "app_package", "${variant.applicationId}"
		}
	}
}
