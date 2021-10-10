package net.twisterrob.gradle.android

import net.twisterrob.gradle.base.BaseExposedPlugin
import net.twisterrob.gradle.java.JavaPlugin
import net.twisterrob.gradle.vcs.VCSPlugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

class AndroidLibraryPlugin : BaseExposedPlugin() {

	override fun apply(target: Project) {
		super.apply(target)

		project.apply<AndroidLifecyclePlugin>()
		project.plugins.apply("com.android.library")

		project.apply<VCSPlugin>()
		project.apply<JavaPlugin>()
		project.apply<AndroidBuildPlugin>()
		//project.apply<AndroidVersionPlugin>()
		//project.apply<AndroidSigningPlugin>()
		project.apply<AndroidMinificationPlugin>()
	}
}
