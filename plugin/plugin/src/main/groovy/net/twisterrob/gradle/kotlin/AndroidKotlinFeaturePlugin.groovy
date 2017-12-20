package net.twisterrob.gradle.kotlin

import net.twisterrob.gradle.android.AndroidFeaturePlugin
import org.gradle.api.Project

class AndroidKotlinFeaturePlugin extends AndroidFeaturePlugin {
	@Override
	void apply(Project target) {
		super.apply(target)
		project.apply plugin: KotlinPlugin
	}
}
