package net.twisterrob.gradle.android

import net.twisterrob.gradle.common.BaseExposedPluginForKotlin
import net.twisterrob.gradle.java.JavaPlugin
import net.twisterrob.gradle.vcs.VCSPlugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

class AndroidAppPlugin : BaseExposedPluginForKotlin() {
	override fun apply(target: Project) {
		super.apply(target)

		project.plugins.apply("com.android.application")

		project.apply<VCSPlugin>()
		project.apply<JavaPlugin>()
		project.apply<AndroidBuildPlugin>()
		project.apply<AndroidSigningPlugin>()
		project.apply<AndroidProguardPlugin>()
		project.apply<AndroidVersionPlugin>()
		project.apply<AndroidReleasePlugin>() // after build, version
	}
}
