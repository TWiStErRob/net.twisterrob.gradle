package net.twisterrob.gradle.checkstyle

import com.android.build.gradle.AppExtension
import com.android.build.gradle.FeatureExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.TestExtension
import net.twisterrob.gradle.common.BaseExposedPlugin
import org.gradle.api.Project

class CheckStylePlugin extends BaseExposedPlugin {

	@Override
	void apply(Project target) {
		super.apply(target)

		project.plugins.withId('com.android.application') {
			def android = project.extensions['android'] as AppExtension
			new CheckStyleTaskCreator(project).applyTo(android.applicationVariants)
		}
		project.plugins.withId('com.android.library') {
			def android = project.extensions['android'] as LibraryExtension
			new CheckStyleTaskCreator(project).applyTo(android.libraryVariants)
		}
		project.plugins.withId('com.android.feature') {
			def android = project.extensions['android'] as FeatureExtension
			new CheckStyleTaskCreator(project).applyTo(android.libraryVariants)
		}
		project.plugins.withId('com.android.test') {
			def android = project.extensions['android'] as TestExtension
			new CheckStyleTaskCreator(project).applyTo(android.applicationVariants)
		}
		project.plugins.withId('com.android.instantapp') {
			// has no variants
			//def android = project.extensions['android'] as InstantAppExtension
		}
	}
}
