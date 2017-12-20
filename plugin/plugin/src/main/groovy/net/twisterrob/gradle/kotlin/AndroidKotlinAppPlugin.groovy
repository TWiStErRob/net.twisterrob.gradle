package net.twisterrob.gradle.kotlin

import net.twisterrob.gradle.android.AndroidAppPlugin
import org.gradle.api.Project

class AndroidKotlinAppPlugin extends AndroidAppPlugin {
	@Override
	void apply(Project target) {
		super.apply(target)
		project.apply plugin: KotlinPlugin
	}
}
