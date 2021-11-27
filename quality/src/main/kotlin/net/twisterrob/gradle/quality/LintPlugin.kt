package net.twisterrob.gradle.quality

import net.twisterrob.gradle.common.BasePlugin
import net.twisterrob.gradle.dsl.contains
import net.twisterrob.gradle.quality.tasks.GlobalLintGlobalFinalizerTask
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

internal open class LintPlugin : BasePlugin() {

	override fun apply(target: Project) {
		super.apply(target)

		if ("lint" !in project.tasks) {
			project.tasks.register("lint", GlobalLintGlobalFinalizerTask)
		}
	}
}
