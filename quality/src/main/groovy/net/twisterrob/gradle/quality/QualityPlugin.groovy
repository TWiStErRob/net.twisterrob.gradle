package net.twisterrob.gradle.quality

import net.twisterrob.gradle.checkstyle.CheckStylePlugin
import net.twisterrob.gradle.common.BaseExposedPlugin
import net.twisterrob.gradle.pmd.PmdPlugin
import org.gradle.api.Project

class QualityPlugin extends BaseExposedPlugin {

	@Override
	void apply(Project target) {
		super.apply(target)

		project.apply plugin: CheckStylePlugin
		project.apply plugin: PmdPlugin
	}
}
