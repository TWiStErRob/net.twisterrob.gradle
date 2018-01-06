package net.twisterrob.gradle.test

import net.twisterrob.gradle.common.BaseExposedPlugin
import org.gradle.api.Project

import java.util.jar.Manifest

class TestPlugin extends BaseExposedPlugin {

	@Override
	void apply(Project target) {
		super.apply(target)
		project.apply plugin: 'java-gradle-plugin'

		project.dependencies.with {
			add('implementation', localGroovy())
			add('implementation', gradleApi())

			add('testImplementation', gradleTestKit())
			add('testImplementation', "net.twisterrob.gradle:test:${getVersion()}")
		}
	}

	String getVersion() {
		URL res = getClass().getResource(getClass().getSimpleName() + ".class")
		JarURLConnection conn = res.openConnection() as JarURLConnection
		Manifest mf = conn.getManifest()
		return mf.getMainAttributes().getValue("Implementation-Version")
	}
}
