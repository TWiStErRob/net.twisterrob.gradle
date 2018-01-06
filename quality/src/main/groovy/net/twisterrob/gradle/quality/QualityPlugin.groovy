package net.twisterrob.gradle.quality

import net.twisterrob.gradle.checkstyle.CheckStylePlugin
import net.twisterrob.gradle.common.BaseExposedPlugin
import org.gradle.api.Project

class QualityPlugin extends BaseExposedPlugin {

	@Override
	void apply(Project target) {
		super.apply(target)

		project.apply plugin: CheckStylePlugin
	}
}
