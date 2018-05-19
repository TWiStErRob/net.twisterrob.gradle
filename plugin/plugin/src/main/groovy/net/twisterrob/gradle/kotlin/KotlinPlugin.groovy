package net.twisterrob.gradle.kotlin

import com.android.build.gradle.BaseExtension
import net.twisterrob.gradle.Utils
import net.twisterrob.gradle.common.BasePlugin
import org.gradle.api.Project

class KotlinPlugin extends BasePlugin {

	private static final String VERSION_KOTLIN = "1.2.41"

	@Override
	void apply(Project target) {
		super.apply(target)

		if (Utils.hasAndroid(project)) {
			// CONSIDER https://github.com/griffio/dagger2-kotlin/blob/master/README.md
			project.apply plugin: 'kotlin-android'
			project.apply plugin: 'kotlin-kapt'
			project.dependencies.implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$VERSION_KOTLIN"
			project.dependencies.testImplementation "org.jetbrains.kotlin:kotlin-test:$VERSION_KOTLIN"

			BaseExtension android = project.android
			android.sourceSets.all {
				it.java.srcDir "src/${it.name}/kotlin"
			}
		} else {
			project.apply plugin: 'kotlin'
			project.dependencies.implementation "org.jetbrains.kotlin:kotlin-stdlib:$VERSION_KOTLIN"
			project.dependencies.testImplementation "org.jetbrains.kotlin:kotlin-test:$VERSION_KOTLIN"
		}
	}
}
