package net.twisterrob.gradle.common

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.TestExtension
import org.gradle.api.Action
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByName

class AndroidVariantApplier(val project: Project) {

	fun applyVariants(
		@Suppress("DEPRECATION" /* AGP 7.0 */)
		variantsClosure: Action<DomainObjectSet<out com.android.build.gradle.api.BaseVariant>>
	) {
		project.plugins.withId("com.android.application") {
			val android = project.extensions.getByName<AppExtension>("android")
			variantsClosure.execute(android.applicationVariants)
		}
		project.plugins.withId("com.android.library") {
			val android = project.extensions.getByName<LibraryExtension>("android")
			variantsClosure.execute(android.libraryVariants)
		}
		project.plugins.withId("com.android.feature") {
			// These types of feature modules were deprecated and removed in AGP 4.x.
			//val android = project.extensions["android"] as FeatureExtension
			//variantsClosure.execute(android.featureVariants)
		}
		project.plugins.withId("com.android.dynamic-feature") {
			val android = project.extensions.getByName<AppExtension>("android")
			variantsClosure.execute(android.applicationVariants)
		}
		project.plugins.withId("com.android.test") {
			val android = project.extensions.getByName<TestExtension>("android")
			variantsClosure.execute(android.applicationVariants)
		}
		project.plugins.withId("com.android.instantapp") {
			//val android = project.extensions.getByName<InstantAppExtension>("android")
			// has no variants, but don't call back, because there's no way to tell if this happened
			//variantsClosure.execute(new DefaultDomainObjectSet<>(BaseVariant))
		}
	}
}
