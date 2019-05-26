package net.twisterrob.gradle.android.tasks

import com.android.annotations.VisibleForTesting
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.internal.TaskManager
import com.android.xml.AndroidXPathFactory
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.getByName
import org.xml.sax.InputSource
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants

open class AndroidInstallRunnerTask : Exec() {
	private var variant: ApkVariant? = null

	init {
		group = TaskManager.INSTALL_GROUP
		description = "Installs the APK for a variant, and then runs the main launcher activity."
		onlyIf { this@AndroidInstallRunnerTask.variant != null }
	}

	fun setVariant(variant: ApkVariant) {
		this.variant = variant
		description = "Installs the APK for ${variant.description}, and then runs the main launcher activity."
	}

	override fun exec() {
		val variant = variant!! // protected by onlyIf
		val output = variant.outputs.single()
		val manifestFile = output
			.processManifestProvider.get()
			.manifestOutputDirectory.get()
			.file("AndroidManifest.xml")
			.asFile
		val activityClass = getMainActivity(manifestFile)!!
		val android: BaseExtension = project.extensions.getByName<BaseExtension>("android")
		// doesn't work: setCommandLine(android.adbExe, "shell", "am", "start", "-a", "android.intent.action.MAIN", "-c", "android.intent.category.LAUNCHER", variant.applicationId)
		setCommandLine(android.adbExecutable, "shell", "am", "start", "-n", "${variant.applicationId}/${activityClass}")
		// or setCommandLine(android.adbExe, "shell", "monkey", "-p", "${variant.applicationId}", "1")
		super.exec()
	}

	companion object {

		@VisibleForTesting
		internal fun getMainActivity(file: File): String? {
			val document = DocumentBuilderFactory
				.newInstance().apply { isNamespaceAware = true }
				.newDocumentBuilder()
				.parse(InputSource(file.inputStream()))
			val xpath = """
				/manifest
				/application
				/activity[
					intent-filter[
						category[@android:name='android.intent.category.LAUNCHER']
						and
						action[@android:name='android.intent.action.MAIN']
					]
				]
				/@android:name
			"""
			val mainActivityName = AndroidXPathFactory // registers `android:`
				.newXPath()
				.evaluate(xpath, document, XPathConstants.STRING) as String
			// XPath returns "" if none found, convert to null
			return mainActivityName.takeUnless { it.isEmpty() }
		}
	}
}
