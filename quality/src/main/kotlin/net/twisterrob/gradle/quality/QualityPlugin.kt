package net.twisterrob.gradle.quality

import net.twisterrob.gradle.checkstyle.CheckStylePlugin
import net.twisterrob.gradle.common.BaseExposedPlugin
import net.twisterrob.gradle.pmd.PmdPlugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

class QualityPlugin : BaseExposedPlugin() {

	override fun apply(target: Project) {
		super.apply(target)

		project.apply<CheckStylePlugin>()
		project.apply<PmdPlugin>()
	}
}
