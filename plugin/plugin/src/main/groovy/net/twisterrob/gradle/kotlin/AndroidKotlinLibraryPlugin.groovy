package net.twisterrob.gradle.kotlin

import net.twisterrob.gradle.android.AndroidLibraryPlugin
import org.gradle.api.Project

class AndroidKotlinLibraryPlugin extends AndroidLibraryPlugin {
	@Override
	void apply(Project target) {
		super.apply(target)
		project.apply plugin: KotlinPlugin
	}
}
