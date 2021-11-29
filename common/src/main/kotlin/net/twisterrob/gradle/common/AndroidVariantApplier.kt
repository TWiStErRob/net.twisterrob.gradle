package net.twisterrob.gradle.common

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.TestExtension
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer

class AndroidVariantApplier(val project: Project) {

	fun applyAfterPluginConfigured(pluginClosure: Action<Plugin<Project>>) {
		val callback = Action { plugin: Plugin<*> ->
			@Suppress("UNCHECKED_CAST") // withId ensures we have BasePlugin, so let's smart cast for further usage
			plugin as Plugin<Project>
			if (project.state.executed) {
				// if state is executed the project has been evaluated so everything is already configured
				// This is required because project.afterEvaluate is not executing Closure after this is true:
				// Notice how org.gradle.configuration.project.LifecycleProjectEvaluator.EvaluateProject.run
				// only runs LifecycleProjectEvaluator.NotifyAfterEvaluate.run once.
				pluginClosure.execute(plugin)
			} else {
				// afterEvaluate ensures that all tasks, variants, etc. are already configured
				project.afterEvaluate { pluginClosure.execute(plugin) }
			}
		}
		project.plugins.withId("com.android.application", callback)
		project.plugins.withId("com.android.library", callback)
		// These types of feature modules were deprecated and removed in AGP 4.x.
		//project.plugins.withId("com.android.feature", callback)
		project.plugins.withId("com.android.dynamic-feature", callback)
		project.plugins.withId("com.android.test", callback)
		if (AGPVersions.CLASSPATH < AGPVersions.v36x) {
			project.plugins.withId("com.android.instantapp", callback)
		}
	}

	fun applyVariants(variantsClosure: Action<Variants>) {
		project.plugins.withId("com.android.application") {
			val android = project.extensions["android"] as AppExtension
			variantsClosure.execute(android.applicationVariants)
		}
		project.plugins.withId("com.android.library") {
			val android = project.extensions["android"] as LibraryExtension
			variantsClosure.execute(android.libraryVariants)
		}
		project.plugins.withId("com.android.feature") {
			// These types of feature modules were deprecated and removed in AGP 4.x.
			//val android = project.extensions["android"] as FeatureExtension
			//variantsClosure.execute(android.featureVariants)
		}
		project.plugins.withId("com.android.dynamic-feature") {
			val android = project.extensions["android"] as AppExtension
			variantsClosure.execute(android.applicationVariants)
		}
		project.plugins.withId("com.android.test") {
			val android = project.extensions["android"] as TestExtension
			variantsClosure.execute(android.applicationVariants)
		}
		project.plugins.withId("com.android.instantapp") {
			//val android = project.extensions["android"] as InstantAppExtension
			// has no variants, but don't call back, because there's no way to tell if this happened
			//variantsClosure.execute(new DefaultDomainObjectSet<>(BaseVariant))
		}
	}

	@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
	fun applyRaw(closure: Action<Void>) {
		project.plugins.withId("com.android.application") {
			closure.execute(null)
		}
		project.plugins.withId("com.android.library") {
			closure.execute(null)
		}
		project.plugins.withId("com.android.feature") {
			// These types of feature modules were deprecated and removed in AGP 4.x.
			//closure.execute(null)
		}
		project.plugins.withId("com.android.dynamic-feature") {
			closure.execute(null)
		}
		project.plugins.withId("com.android.test") {
			closure.execute(null)
		}
		project.plugins.withId("com.android.instantapp") {
			//val android = project.extensions["android"] as InstantAppExtension
			// has no variants, but don't call back, because there's no way to tell if this happened
			//closure.execute(new DefaultDomainObjectSet<>(BaseVariant))
		}
	}

	/**
	 * Note: due to the way {@link DomainObjectSet#all} works,
	 * {@code after} is not really after all items have been processed.
	 */
	fun applyAll(
		variantClosure: Action<in BaseVariant>,
		before: Action<Variants> = NOOP,
		after: Action<Variants> = NOOP
	) {
		applyVariants { variants: Variants ->
			before.execute(variants)
			variants.all(variantClosure)
			after.execute(variants)
		}
	}
}

private val NOOP: Action<Variants> = Action { }

private operator fun ExtensionContainer.get(name: String): Any =
	getByName(name)
