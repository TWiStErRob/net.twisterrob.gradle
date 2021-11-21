package net.twisterrob.gradle.root

import net.twisterrob.gradle.base.BaseExposedPlugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

class RootPlugin : BaseExposedPlugin() {

	override fun apply(target: Project) {
		super.apply(target)

		project.apply<GradlePlugin>()
	}
}
