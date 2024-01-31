package net.twisterrob.gradle.vcs.git

import net.twisterrob.gradle.common.BasePlugin
import net.twisterrob.gradle.kotlin.dsl.extensions
import net.twisterrob.gradle.vcs.VCSPluginExtension.Companion.vcs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

@Suppress("detekt.UnnecessaryAbstractClass") // Gradle convention.
abstract class GitPlugin : BasePlugin() {

	override fun apply(target: Project) {
		super.apply(target)
		project.vcs.extensions.create<GitPluginExtension>(GitPluginExtension.NAME, project.rootDir)
	}
}
