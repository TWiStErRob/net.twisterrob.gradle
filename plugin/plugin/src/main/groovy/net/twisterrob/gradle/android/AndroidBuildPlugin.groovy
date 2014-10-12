package net.twisterrob.gradle.android

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariant
import com.android.builder.core.DefaultApiVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.DefaultDomainObjectSet

class AndroidBuildPluginExtension {
	boolean decorateBuildConfig = true
	boolean addRunTasks = true
}

public class AndroidBuildPlugin implements Plugin<Project> {
	private BaseExtension android

	@Override
	void apply(Project project) {
		android = project.android
		def twisterrob = android.extensions.create('twisterrob', AndroidBuildPluginExtension)

		android.with {
			lintOptions.with {
				xmlReport = false
				checkAllWarnings = true
				disable 'Assert'
			}
			buildToolsVersion "20" // latest Android SDK Build-tools
			compileSdkVersion "android-19" // Android 4.4.2/SDK Platform

			defaultConfig.with {
				//minSdkVersion = new DefaultApiVersion(10) // 2.3.3 Gingerbread MR1
				setTargetSdkVersion(new DefaultApiVersion(19)) // 4.4.2 KitKat
				buildConfigField "String", "EMAIL", "\"feedback@twisterrob.net\""
			}

			buildTypes['debug'].with {
				setApplicationIdSuffix(".debug")
				setVersionNameSuffix("-DEBUG")
				buildConfigField "String", "EMAIL", "\"papp.robert.s@gmail.com\""
			}
		}

		project.plugins.withType(AppPlugin) {
			project.afterEvaluate {
				def tasks = project.tasks
				if (twisterrob.decorateBuildConfig) {
					tasks.create('decorateBuildConfig', DecorateBuildConfigTask) {
						description 'Add more information about build to BuildConfig.java'
						tasks.preBuild.dependsOn delegate
					}

					android.applicationVariants.all { ApplicationVariant variant ->
						addPackageName(variant)
					}
				}

				if (twisterrob.addRunTasks) {
					android.applicationVariants.all { ApkVariant variant ->
						if (variant.install) {
							variant.install.project.tasks.create(
									name: "run${variant.name.capitalize()}",
									type: AndroidInstallRunner,
									dependsOn: variant.install
							).variant = variant
						}
					}
				}
			}
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
