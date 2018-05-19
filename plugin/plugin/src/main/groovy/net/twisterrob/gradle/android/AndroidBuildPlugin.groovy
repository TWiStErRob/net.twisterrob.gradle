package net.twisterrob.gradle.android

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.dsl.BuildType
import com.android.build.gradle.internal.variant.BaseVariantData
import com.android.builder.core.DefaultApiVersion
import net.twisterrob.gradle.Utils
import net.twisterrob.gradle.android.tasks.AndroidInstallRunnerTask
import net.twisterrob.gradle.android.tasks.DecorateBuildConfigTask
import net.twisterrob.gradle.common.BasePlugin
import org.gradle.api.Project

class AndroidBuildPluginExtension {

	boolean decorateBuildConfig = true
	boolean addRunTasks = true
}

class AndroidBuildPlugin extends BasePlugin {

	/**
	 * Keep it in sync with AppCompat's minimum.
	 */
	public static final int VERSION_SDK_MINIMUM = 14
	/**
	 * Latest SDK version available, Google Play Store has stringent rules, so keep up to date.
	 */
	public static final int VERSION_SDK_TARGET = 27
	/**
	 * Latest SDK version available, useful for discovering deprecated methods and getting new features like `.findViewById<T>()`.
	 */
	public static final int VERSION_SDK_COMPILE = VERSION_SDK_TARGET
	public static final String VERSION_SDK_COMPILE_NAME = "8.1.0" // Oreo
	/**
	 * Latest build tools version available, there's no reason to hold back.
	 */
	public static final String VERSION_BUILD_TOOLS = "27.0.3"

	private BaseExtension android

	@Override
	void apply(Project target) {
		super.apply(target)
		android = project.android

		def twisterrob = android.extensions.create('twisterrob', AndroidBuildPluginExtension)

		// :lintVitalRelease trying to resolve :lintClassPath that has Groovy, Kotlin and some libs
		// otherwise default maven repo that's a superset of mavenCentral(), so prefer this
		project.repositories.jcenter() // http://jcenter.bintray.com/
		// most of Android's stuff is distributed here, so add by default
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
			buildToolsVersion VERSION_BUILD_TOOLS
			compileSdkVersion "android-$VERSION_SDK_COMPILE"

			defaultConfig.with {
				setMinSdkVersion(new DefaultApiVersion(VERSION_SDK_MINIMUM))
				setTargetSdkVersion(new DefaultApiVersion(VERSION_SDK_TARGET))
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

	static void addPackageName(BaseVariant variant) {
		// Package name for use e.g. in preferences to launch intent from the right package or for content providers
		variant.resValue "string", "app_package", variant.applicationId
	}
}
