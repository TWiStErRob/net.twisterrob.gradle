package net.twisterrob.gradle.android.tasks

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.TaskManager
import com.android.xml.AndroidXPathFactory
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.kotlin.dsl.getByName
import org.gradle.work.DisableCachingByDefault
import org.intellij.lang.annotations.Language
import org.jetbrains.annotations.VisibleForTesting
import org.xml.sax.InputSource
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants

@DisableCachingByDefault(because = "Has a side effect on the device.")
abstract class AndroidInstallRunnerTask : Exec() {

	@get:Input
	abstract val applicationId: Property<String>

	@get:InputFile
	@get:PathSensitive(PathSensitivity.RELATIVE)
	abstract val manifestFile: RegularFileProperty

	@get:InputFile
	@get:PathSensitive(PathSensitivity.ABSOLUTE)
	abstract val adbExecutable: RegularFileProperty

	init {
		group = TaskManager.INSTALL_GROUP
		description = "Installs the APK for a variant, and then runs the main launcher activity."
		// Always execute as device state cannot be used by Gradle for up-to-date check.
		outputs.upToDateWhen { false }
		@Suppress("LeakingThis")
		adbExecutable.fileProvider(project.provider {
			val android: BaseExtension = project.extensions.getByName<BaseExtension>("android")
			android.adbExecutable
		})
	}

	fun updateDescription(variant: String) {
		description = "Installs the APK for ${variant}, and then runs the main launcher activity."
	}

	override fun exec() {
		val activityClass = getMainActivity(manifestFile.get().asFile.inputStream())
			?: error("Cannot get MAIN/LAUNCHER activity from ${manifestFile.get()}.")
		// doesn't work: setCommandLine(android.adbExe, "shell", "am", "start", "-a", "android.intent.action.MAIN", "-c", "android.intent.category.LAUNCHER", variant.applicationId)
		setCommandLine(adbExecutable.get(), "shell", "am", "start", "-n", "${applicationId.get()}/${activityClass}")
		// or setCommandLine(android.adbExe, "shell", "monkey", "-p", "${variant.applicationId}", "1")
		super.exec()
	}

	companion object {

		@VisibleForTesting
		internal fun getMainActivity(androidManifest: InputStream): String? {
			val document = DocumentBuilderFactory
				.newInstance()
				.apply { isNamespaceAware = true }
				.newDocumentBuilder()
				.parse(InputSource(androidManifest))
			@Language("XPath")
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
			""".trimIndent()
			val mainActivityName = AndroidXPathFactory // registers `android:`
				.newXPath()
				.evaluate(xpath, document, XPathConstants.STRING) as String
			// XPath returns "" if none found, convert to null
			return mainActivityName.takeUnless { it.isEmpty() }
		}
	}
}
