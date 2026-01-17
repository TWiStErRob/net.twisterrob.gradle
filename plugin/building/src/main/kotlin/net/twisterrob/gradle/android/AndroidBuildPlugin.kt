package net.twisterrob.gradle.android

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.variant.ApplicationVariant
import com.android.build.api.variant.ResValue
import com.android.build.api.variant.Variant
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BasePlugin
import com.android.build.gradle.internal.component.ComponentCreationConfig
import com.android.build.gradle.internal.dsl.BuildType
import net.twisterrob.gradle.android.tasks.AndroidInstallRunnerTask
import net.twisterrob.gradle.android.tasks.CalculateBuildTimeTask
import net.twisterrob.gradle.android.tasks.CalculateBuildTimeTask.Companion.addBuildConfigFields
import net.twisterrob.gradle.android.tasks.CalculateVCSRevisionInfoTask
import net.twisterrob.gradle.android.tasks.CalculateVCSRevisionInfoTask.Companion.addBuildConfigFields
import net.twisterrob.gradle.base.shouldAddAutoRepositoriesTo
import net.twisterrob.gradle.internal.android.description
import net.twisterrob.gradle.internal.android.taskContainer
import net.twisterrob.gradle.kotlin.dsl.extensions
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import java.util.Locale

@Suppress("detekt.UnnecessaryAbstractClass") // Gradle convention.
abstract class AndroidBuildPluginExtension {

	var isDecorateBuildConfig: Boolean = true

	companion object {

		internal const val NAME: String = "twisterrob"
	}
}

@Suppress("detekt.UnnecessaryAbstractClass") // Gradle convention.
abstract class AndroidBuildPlugin : net.twisterrob.gradle.common.BasePlugin() {

	override fun apply(target: Project) {
		super.apply(target)
		@Suppress("DEPRECATION" /* AGP 9.0 */)
		val android = project.extensions.getByName<com.android.build.gradle.BaseExtension>("android")
		val twisterrob = android.extensions.create<AndroidBuildPluginExtension>(AndroidBuildPluginExtension.NAME)

		if (shouldAddAutoRepositoriesTo(project)) {
			// most of Android's stuff is distributed here, so add by default
			project.repositories.google() // https://maven.google.com
			// :lintVitalRelease trying to resolve :lintClassPath that has Groovy, Kotlin and some libs
			project.repositories.mavenCentral() // https://repo.maven.apache.org/maven2/
		}

		@Suppress("NestedScopeFunctions")
		with(android) {
			configureLint()
			compileSdkVersion = "android-${VERSION_SDK_COMPILE}"

			with(defaultConfig) {
				minSdk = VERSION_SDK_MINIMUM
				targetSdk = VERSION_SDK_TARGET
				vectorDrawables.useSupportLibrary = true
			}
			with(buildTypes) {
				configureSuffixes(project)
				configureBuildResValues()
			}
			with(packagingOptions) {
				resources.excludes.addAll(knownUnneededFiles())
			}
			decorateBuildConfig(project, twisterrob)
		}

		project.plugins.withType<AppPlugin>().configureEach {
			if (twisterrob.isDecorateBuildConfig) {
				project.androidComponentsApplication.onVariants { variant ->
					addPackageName(variant)
				}
			}
		}
		project.plugins.withType<BasePlugin>().configureEach {
			project.androidComponents.onVariants { variant ->
				// This needs to be inside onVariants,
				// because it's not possible to register onVariants callback in afterEvaluate.
				project.afterEvaluate {
					// This has to be called in afterEvaluate, to make sure that Tasks are created for variants.
					// See VariantManager.hasCreatedTasks for when this happens.
					fixVariantTaskGroups(variant)
				}
			}
		}
		project.plugins.withType<AppPlugin>().configureEach {
			project.androidComponentsApplication.onVariants { variant ->
				registerRunTask(project, variant)
			}
		}
	}

	companion object {

		@Suppress("DEPRECATION" /* AGP 9.0 */)
		private fun com.android.build.gradle.BaseExtension.configureLint() {
			(this as CommonExtension<*, *, *, *, *, *>).lint.apply {
				xmlReport = false
				checkAllWarnings = true
				abortOnError = true
				disable.add("Assert")
				disable.add("GoogleAppIndexingWarning")
				fatal.add("StopShip") // http://stackoverflow.com/q/33504186/253468
			}
		}

		private fun NamedDomainObjectContainer<out BuildType>.configureSuffixes(project: Project) {
			configure("debug") { debug ->
				project.plugins.withType<AppPlugin>().configureEach {
					// TODO make debug buildTypes configurable, use name of buildType as suffix
					debug.setApplicationIdSuffix(".${debug.name}")
				}
				debug.setVersionNameSuffix("d")
			}
			@Suppress("UNUSED_ANONYMOUS_PARAMETER") // Keep for reference.
			configure("release") { release ->
				//release.setApplicationIdSuffix(null)
				//release.setVersionNameSuffix(null)
			}
		}

		private fun NamedDomainObjectContainer<out BuildType>.configureBuildResValues() {
			configure("debug") { debug ->
				debug.resValue("bool", "in_test", "true")
				debug.resValue("bool", "in_prod", "false")
			}

			configure("release") { release ->
				release.resValue("bool", "in_test", "false")
				release.resValue("bool", "in_prod", "true")
			}
		}

		/**
		 * Configure files we don't need in APKs.
		 */
		fun knownUnneededFiles(): List<String> =
			listOf(
				// support-annotations-28.0.0.jar contains this file
				// it's for Android Gradle Plugin at best, if at all used
				"META-INF/proguard/androidx-annotations.pro",

				// Each Android Support Library component has a separate entry for storing version.
				// Probably used by Google Play to do statistics, gracefully opt out of this.
				"META-INF/android.*.version",
				"META-INF/androidx.*.version",

				// Needed for compiling against top-level functions. Since APK is end product, this is not necessary.
				// TODO consider not including this for AARs.
				"**/*.kotlin_metadata",
				// kotlin_module file holds @OptionalExpectation, @JvmMultifileClass, module annotations.
				//"**/*.kotlin_module",
				// kotlin_builtins are required for org.jetbrains.kotlin:kotlin-reflect.
				//"**/*.kotlin_builtins",

				// Readmes
				// (e.g. hamcrest-library-2.1.jar and hamcrest-core-2.1.jar both pack a readme to encourage upgrade)
				"**/README.txt",
				"**/README.md",
			)

		@Suppress("DEPRECATION" /* AGP 9.0 */)
		private fun com.android.build.gradle.BaseExtension.decorateBuildConfig(project: Project, twisterrob: AndroidBuildPluginExtension) {
			val buildTimeTaskProvider =
				project.tasks.register<CalculateBuildTimeTask>("calculateBuildConfigBuildTime")
			val vcsTaskProvider =
				project.tasks.register<CalculateVCSRevisionInfoTask>("calculateBuildConfigVCSRevisionInfo")
			project.androidComponents.finalizeDsl {
				if (twisterrob.isDecorateBuildConfig && buildFeatures.buildConfig == true) {
					project.decorateBuildConfig(buildTimeTaskProvider, vcsTaskProvider)
				}
			}
		}

		/**
		 * This is a new incubating way of adding buildConfigFields introduced in AGP 4.1.
		 * @see https://issuetracker.google.com/issues/172657565
		 * @see https://github.com/android/gradle-recipes/blob/8d0c14d6fed86726df60fb8c8f79e5a03c66fdee/Kotlin/addCustomFieldWithValueFromTask/app/build.gradle.kts
		 */
		private fun Project.decorateBuildConfig(
			buildTimeTaskProvider: TaskProvider<CalculateBuildTimeTask>,
			vcsTaskProvider: TaskProvider<CalculateVCSRevisionInfoTask>
		) {
			buildTimeTaskProvider.addBuildConfigFields(project)
			vcsTaskProvider.addBuildConfigFields(project)
		}

		private fun registerRunTask(project: Project, variant: ApplicationVariant) {
			val variantName = variant.name.replaceFirstChar { it.uppercase(Locale.ROOT) }
			project.tasks.register<AndroidInstallRunnerTask>("run${variantName}") {
				// Delay task retrieval until task graph calculation so AGP has a chance to set up the tasks.
				this.dependsOn(project.provider {
					variant.taskContainer.installTask
						?: error("Install task for variant ${variant.name} is missing.")
				})
				this.manifestFile.set(variant.artifacts.get(SingleArtifact.MERGED_MANIFEST))
				this.applicationId.set(variant.applicationId)
				this.updateDescription(variant.description)
			}
		}

		private fun fixVariantTaskGroups(variant: Variant) {
			fun ComponentCreationConfig.fixMetadata() {
				taskContainer.compileTask.configure { task ->
					// This is now done in TaskManager, at createCompileAnchorTask.
					task.group = org.gradle.api.plugins.BasePlugin.BUILD_GROUP
					task.description = "Compiles sources for ${variant.description}."
				}
				taskContainer.javacTask.configure { task ->
					task.group = org.gradle.api.plugins.BasePlugin.BUILD_GROUP
					task.description = "Compiles Java sources for ${variant.description}."
				}
			}
			variant.components
				.filterIsInstance<ComponentCreationConfig>()
				.forEach(ComponentCreationConfig::fixMetadata)
		}

		private fun addPackageName(variant: ApplicationVariant) {
			val comment = "Package name for use in resources, for example " +
					"in preferences.xml to launch an intent from the right package, " +
					"or for ContentProviders' <provider android:authorities, " +
					"or <searchable android:searchSuggestAuthority."
			variant.resValues.put(
				variant.makeResValueKey("string", "app_package"),
				ResValue(variant.applicationId.get(), comment)
			)
		}
	}
}
