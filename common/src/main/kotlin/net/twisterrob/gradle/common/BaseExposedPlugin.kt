package net.twisterrob.gradle.common

import org.gradle.api.Project

open class BaseExposedPlugin : BasePlugin() {

	override fun apply(target: Project) {
		super.apply(target)

		//project.apply<GradlePlugin>()
	}
}
