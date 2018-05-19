package net.twisterrob.gradle.android

import net.twisterrob.gradle.apply
import net.twisterrob.gradle.common.BaseExposedPluginForKotlin
import net.twisterrob.gradle.java.JavaPlugin
import net.twisterrob.gradle.vcs.VCSPlugin
import org.gradle.api.Project

class AndroidAppPlugin : BaseExposedPluginForKotlin() {
	override fun apply(target: Project) {
		super.apply(target)

		project.plugins.apply("com.android.application")

		project.plugins.apply<VCSPlugin>()
		project.plugins.apply<JavaPlugin>()
		project.plugins.apply<AndroidBuildPlugin>()
		project.plugins.apply<AndroidSigningPlugin>()
		project.plugins.apply<AndroidProguardPlugin>()
		project.plugins.apply<AndroidVersionPlugin>()
		project.plugins.apply<AndroidReleasePlugin>() // after build, version
	}
}
