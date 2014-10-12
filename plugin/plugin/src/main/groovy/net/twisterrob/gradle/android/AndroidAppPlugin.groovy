package net.twisterrob.gradle.android

import net.twisterrob.gradle.common.BaseExposedPlugin
import net.twisterrob.gradle.java.JavaPlugin
import net.twisterrob.gradle.vcs.VCSPlugin
import org.gradle.api.Project

class AndroidAppPlugin extends BaseExposedPlugin {
	@Override
	void apply(Project target) {
		super.apply(target)

		project.apply plugin: 'com.android.application'

		project.apply plugin: VCSPlugin
		project.apply plugin: JavaPlugin
		project.apply plugin: AndroidBuildPlugin
		project.apply plugin: AndroidVersionPlugin
		project.apply plugin: AndroidSigningPlugin
		project.apply plugin: AndroidProguardPlugin
	}
}
