package net.twisterrob.gradle.kotlin

import com.android.build.gradle.BaseExtension
import net.twisterrob.gradle.Utils
import net.twisterrob.gradle.common.BasePlugin
import org.gradle.api.Project

class KotlinPlugin extends BasePlugin {

	@Override
	void apply(Project target) {
		super.apply(target)

		// TODO https://github.com/griffio/dagger2-kotlin/blob/master/README.md
		//project.apply plugin: 'kotlin-kapt'
		if (Utils.hasAndroid(project)) {
			project.apply plugin: 'kotlin-android'
			project.dependencies.implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.2.10"

			BaseExtension android = project.android
			android.sourceSets.all {
				it.java.srcDir "src/${it.name}/kotlin"
			}
		} else {
			project.apply plugin: 'kotlin'
		}
	}
}
