package net.twisterrob.gradle.checkstyle

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import net.twisterrob.gradle.common.BaseExposedPlugin
import org.gradle.api.Project

class CheckStylePlugin extends BaseExposedPlugin {

	@Override
	void apply(Project target) {
		super.apply(target)

		if (project == project.rootProject) {
			// root project needs to apply to all
			applyRecursively(project)
		} else {
			// subprojects need not recurse, just apply to this 
			applyTo(project)
		}
	}

	private void applyRecursively(Project project) {
		project.subprojects.each this.&applyRecursively
		applyTo(project)
	}

	private static void applyTo(Project project) {
		project.plugins.withId('com.android.application') {
			def android = project.extensions['android'] as AppExtension
			new CheckStyleTaskCreator(project).applyTo(android.applicationVariants)
		}
		project.plugins.withId('com.android.library') {
			def android = project.extensions['android'] as LibraryExtension
			new CheckStyleTaskCreator(project).applyTo(android.libraryVariants)
		}
	}
}
