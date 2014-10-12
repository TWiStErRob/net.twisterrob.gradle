package net.twisterrob.gradle.android

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

public class DecorateBuildConfigTask extends DefaultTask {
	def buildConfigField

	DecorateBuildConfigTask() {
		buildConfigField = project.android.defaultConfig.&buildConfigField
	}

	@TaskAction
	void addVCSInformation() {
		buildConfigField "String", "REVISION", "\"${project.VCS.current.revision}\""
		buildConfigField "int", "REVISION_NUMBER", "${project.VCS.current.revisionNumber}"
	}

	@TaskAction
	void addBuildInfo() {
		def buildTime = new Date().time
		buildConfigField "java.util.Date", "BUILD_TIME",
				"new java.util.Date(${buildTime}L) /* ${dateFormat(buildTime)} *" + "/"
	}

	static String dateFormat(long date) {
		return new Date(date).format("yyyy-MM-dd'T'HH:mm:ss'Z'", TimeZone.getTimeZone("UTC"))
	}
}
