package net.twisterrob.gradle.common

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BasePlugin
import com.android.build.gradle.FeatureExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.TestExtension
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.Action
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project

class AndroidVariantApplier {

	private Project project

	AndroidVariantApplier(Project project) {
		this.project = project
	}

	void applyAfterPluginConfigured(Action<BasePlugin> pluginClosure) {
		def callback = {BasePlugin plugin ->
			// withId ensures we have BasePlugin
			project.afterEvaluate {
				// afterEvaluate ensures that all tasks, variants, etc are already configured
				pluginClosure.execute(plugin)
			}
		}
		project.plugins.withId('com.android.application', callback)
		project.plugins.withId('com.android.library', callback)
		project.plugins.withId('com.android.feature', callback)
		project.plugins.withId('com.android.test', callback)
		project.plugins.withId('com.android.instantapp', callback)
	}

	void apply(Action<DomainObjectSet<? extends BaseVariant>> variantsClosure) {
		project.plugins.withId('com.android.application') {
			def android = project.extensions['android'] as AppExtension
			variantsClosure.execute(android.applicationVariants)
		}
		project.plugins.withId('com.android.library') {
			def android = project.extensions['android'] as LibraryExtension
			variantsClosure.execute(android.libraryVariants)
		}
		project.plugins.withId('com.android.feature') {
			def android = project.extensions['android'] as FeatureExtension
			variantsClosure.execute(android.libraryVariants)
		}
		project.plugins.withId('com.android.test') {
			def android = project.extensions['android'] as TestExtension
			variantsClosure.execute(android.applicationVariants)
		}
		project.plugins.withId('com.android.instantapp') {
			//def android = project.extensions['android'] as InstantAppExtension
			// has no variants, but don't call back, because there's no way to tell if this happened
			//variantsClosure.execute(new DefaultDomainObjectSet<>(BaseVariant))
		}
	}

	/**
	 * Note: due to the way {@link DomainObjectSet#all} works,
	 * {@code after} is not really after all items have been processed.
	 */
	void applyAll(Action<? extends BaseVariant> variantClosure,
			Action<DomainObjectSet<? extends BaseVariant>> before = Closure.IDENTITY as Action,
			Action<DomainObjectSet<? extends BaseVariant>> after = Closure.IDENTITY as Action) {
		apply({DomainObjectSet<? extends BaseVariant> variants ->
			before.execute(variants)
			variants.all variantClosure
			after.execute(variants)
		})
	}
}
