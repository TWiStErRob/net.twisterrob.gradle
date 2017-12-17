package net.twisterrob.gradle.android.tasks

import com.android.build.gradle.BaseExtension
import net.twisterrob.gradle.vcs.VCSPluginExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.util.concurrent.TimeUnit

class DecorateBuildConfigTask extends DefaultTask {
	private static final long HALF_HOUR = TimeUnit.MINUTES.toMillis(30)
	def buildConfigField/*(String type, String name, String value)*/
	boolean enableVCS = true
	boolean enableBuild = true
	/**
	 * Default implementation returns a half hour precision time
	 * to minimize {@code compile*JavaWithJavac} rebuilds due to a single number change in BuildConfig.java.
	 *
	 * It can be overridden like this:
	 * <pre><code>tasks.decorateBuildConfig.configure { getBuildTime = { System.currentTimeMillies() }}</code></pre>
	 *
	 * @returns a long representing the UTC time of the build.
	 */
	def getBuildTime = { System.currentTimeMillis().intdiv(HALF_HOUR) * HALF_HOUR }

	DecorateBuildConfigTask() {
		BaseExtension android = project.android
		buildConfigField = android.defaultConfig.&buildConfigField
	}

	@TaskAction
	void addVCSInformation() {
		if (enableVCS) {
			def vcs = project.VCS as VCSPluginExtension
			buildConfigField "String", "REVISION", "\"${vcs.current.revision}\""
			buildConfigField "int", "REVISION_NUMBER", "${vcs.current.revisionNumber}"
		}
	}

	@TaskAction
	void addBuildInfo() {
		if (enableBuild) {
			long buildTime = getBuildTime()
			buildConfigField "java.util.Date", "BUILD_TIME",
					"new java.util.Date(${buildTime}L) /* ${dateFormat(buildTime)} */"
		}
	}

	static String dateFormat(long date) {
		return new Date(date).format("yyyy-MM-dd'T'HH:mm:ss'Z'", TimeZone.getTimeZone("UTC"))
	}
}
