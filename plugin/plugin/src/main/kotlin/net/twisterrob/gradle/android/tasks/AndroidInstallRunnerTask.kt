package net.twisterrob.gradle.android.tasks

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.internal.TaskManager
import groovy.util.XmlSlurper
import groovy.util.slurpersupport.Attributes
import groovy.util.slurpersupport.GPathResult
import groovy.util.slurpersupport.NodeChildren
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.getByName
import java.io.File

open class AndroidInstallRunnerTask : Exec() {
	private var variant: ApkVariant? = null

	fun setVariant(variant: ApkVariant) {
		this.variant = variant
		group = TaskManager.INSTALL_GROUP
		description = "Installs the APK for ${variant.description}, and then runs the main launcher activity."
		onlyIf { this@AndroidInstallRunnerTask.variant != null }
	}

	override fun exec() {
		val variant = variant!!
		val output = variant.outputs.single()
		val manifestFileName = "${output.processManifest.manifestOutputDirectory}/AndroidManifest.xml"
		val activityClass = getMainActivity(project.file(manifestFileName))
		val android: BaseExtension = project.extensions.getByName<BaseExtension>("android")
		// doesn't work: setCommandLine(android.adbExe, "shell", "am", "start", "-a", "android.intent.action.MAIN", "-c", "android.intent.category.LAUNCHER", variant.applicationId)
		setCommandLine(android.adbExecutable, "shell", "am", "start", "-n", "${variant.applicationId}/${activityClass}")
		// or setCommandLine(android.adbExe, "shell", "monkey", "-p", "${variant.applicationId}", "1")
		super.exec()
	}

	// TODO use Java/Kotlin API to do this, preferably XPath
	companion object {

		internal fun getMainActivity(file: File): String {
			val xmlRoot = XmlSlurper().parse(file)
			val application = xmlRoot.one("application")
			val activities = application.many("activity")
			val launcherActivity = activities.find(::isAppLauncher)!!
			return launcherActivity.attr("@android:name").text()
		}

		private fun isAppLauncher(activity: GPathResult): Boolean {
			val intentFilters = activity.many("intent-filter")
			return intentFilters.find { intentFilter ->
				val isMain = intentFilter.many("action").find {
					it.attr("@android:name").text() == "android.intent.action.MAIN"
				} != null
				val isLauncher = intentFilter.many("category").find {
					it.attr("@android:name").text() == "android.intent.category.LAUNCHER"
				} != null
				return@find isMain && isLauncher
			} != null
		}

		// GPathResults is amazing not type-safe, so we need some magical help to be able to use it from Kotlin
		private fun GPathResult.many(selector: String) = getProperty(selector) as NodeChildren

		private fun GPathResult.one(selector: String) = getProperty(selector) as GPathResult
		private fun GPathResult.attr(selector: String) = getProperty(selector) as Attributes
		@Suppress("UNCHECKED_CAST") // GPathResult implements Iterable<*>, so need to be more explicit
		private fun GPathResult.find(predicate: (GPathResult) -> Boolean) =
			(this as Iterable<GPathResult>).singleOrNull { predicate(it) }
	}
}
