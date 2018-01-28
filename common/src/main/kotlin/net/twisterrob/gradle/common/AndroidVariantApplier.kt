package net.twisterrob.gradle.common

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BasePlugin
import com.android.build.gradle.FeatureExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.TestExtension
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer

class AndroidVariantApplier(val project: Project) {

	fun applyAfterPluginConfigured(pluginClosure: Action<BasePlugin>) {
		val callback = Action { plugin: Plugin<*> ->
			// afterEvaluate ensures that all tasks, variants, etc. are already configured
			project.afterEvaluate {
				// withId ensures we have BasePlugin
				pluginClosure.execute(plugin as BasePlugin)
			}
		}
		project.plugins.withId("com.android.application", callback)
		project.plugins.withId("com.android.library", callback)
		project.plugins.withId("com.android.feature", callback)
		project.plugins.withId("com.android.test", callback)
		project.plugins.withId("com.android.instantapp", callback)
	}

	fun apply(variantsClosure: Action<Variants>) {
		project.plugins.withId("com.android.application") {
			val android = project.extensions["android"] as AppExtension
			variantsClosure.execute(android.applicationVariants)
		}
		project.plugins.withId("com.android.library") {
			val android = project.extensions["android"] as LibraryExtension
			variantsClosure.execute(android.libraryVariants)
		}
		project.plugins.withId("com.android.feature") {
			val android = project.extensions["android"] as FeatureExtension
			variantsClosure.execute(android.libraryVariants)
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

	/**
	 * Note: due to the way {@link DomainObjectSet#all} works,
	 * {@code after} is not really after all items have been processed.
	 */
	fun applyAll(
			variantClosure: Action<in BaseVariant>,
			before: Action<Variants> = NOOP,
			after: Action<Variants> = NOOP) {
		apply(Action { variants: Variants ->
			before.execute(variants)
			variants.all(variantClosure)
			after.execute(variants)
		})
	}
}

private val NOOP: Action<Variants> = Action { }

private operator fun ExtensionContainer.get(name: String) = getByName(name)
