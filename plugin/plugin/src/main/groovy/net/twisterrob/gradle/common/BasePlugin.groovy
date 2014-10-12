package net.twisterrob.gradle.common

import org.gradle.api.Plugin
import org.gradle.api.Project

class BasePlugin implements Plugin<Project> {
	protected Project project

	@Override
	void apply(Project target) {
		project = target
	}
}
