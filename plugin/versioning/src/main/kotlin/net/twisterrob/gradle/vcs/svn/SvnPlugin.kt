package net.twisterrob.gradle.vcs.svn

import net.twisterrob.gradle.common.BasePlugin
import net.twisterrob.gradle.kotlin.dsl.extensions
import net.twisterrob.gradle.vcs.VCSPluginExtension.Companion.vcs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

@Suppress("detekt.UnnecessaryAbstractClass") // Gradle convention.
abstract class SvnPlugin : BasePlugin() {

	override fun apply(target: Project) {
		super.apply(target)
		val rootDir = project.rootProject.layout.projectDirectory
		project.vcs.extensions.create<SvnPluginExtension>(SvnPluginExtension.NAME, rootDir)
	}
}
