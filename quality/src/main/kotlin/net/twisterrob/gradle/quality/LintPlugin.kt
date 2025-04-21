package net.twisterrob.gradle.quality

import net.twisterrob.gradle.common.BasePlugin
import net.twisterrob.gradle.common.registerTask
import net.twisterrob.gradle.quality.tasks.GlobalLintGlobalFinalizerTask
import org.gradle.api.Project

@Suppress("detekt.UnnecessaryAbstractClass") // Gradle convention.
internal abstract class LintPlugin : BasePlugin() {

	override fun apply(target: Project) {
		super.apply(target)

		if ("lint" !in project.tasks.names) {
			project.registerTask("lint", GlobalLintGlobalFinalizerTask.Creator())
		}
	}
}
