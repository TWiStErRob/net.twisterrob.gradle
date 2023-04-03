package net.twisterrob.gradle.common

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
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
			variantsClosure.execute(android<AppExtension>().applicationVariants)
		}
		project.plugins.withId("com.android.library") {
			variantsClosure.execute(android<LibraryExtension>().libraryVariants)
		}
		project.plugins.withId("com.android.dynamic-feature") {
			variantsClosure.execute(android<AppExtension>().applicationVariants)
		}
		project.plugins.withId("com.android.test") {
			variantsClosure.execute(android<TestExtension>().applicationVariants)
		}
		project.plugins.withId("com.android.instantapp") {
			//android<InstantAppExtension>()
			// has no variants, but don't call back, because there's no way to tell if this happened
			//variantsClosure.execute(new DefaultDomainObjectSet<>(BaseVariant))
		}
	}

	inline fun <reified T : BaseExtension> android(): T =
		project.extensions.getByName<T>("android")
}
