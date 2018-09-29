package net.twisterrob.gradle.android

import net.twisterrob.gradle.base.BaseExposedPlugin
import net.twisterrob.gradle.java.JavaPlugin
import net.twisterrob.gradle.vcs.VCSPlugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

class AndroidAppPlugin : BaseExposedPlugin() {

	override fun apply(target: Project) {
		super.apply(target)

		project.apply<VCSPlugin>()
		project.apply<AndroidVersionPlugin>() // after vcs
		project.plugins.apply("com.android.application")

		project.apply<JavaPlugin>()
		project.apply<AndroidBuildPlugin>()
		project.apply<AndroidSigningPlugin>()
		project.apply<AndroidProguardPlugin>()
		project.apply<AndroidReleasePlugin>() // after build, version
	}
}
