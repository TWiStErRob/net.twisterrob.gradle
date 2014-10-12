package net.twisterrob.gradle.common

import org.gradle.api.Project

class BaseExposedPlugin extends BasePlugin {
	@Override
	void apply(Project target) {
		super.apply(target)

		project.apply plugin: GradlePlugin
	}
}
