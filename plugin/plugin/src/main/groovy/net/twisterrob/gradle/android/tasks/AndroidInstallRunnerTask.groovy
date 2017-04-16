package net.twisterrob.gradle.android.tasks

import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.internal.TaskManager
import groovy.util.slurpersupport.GPathResult
import org.gradle.api.tasks.Exec

class AndroidInstallRunnerTask extends Exec {
	private ApplicationVariant variant

	void setVariant(ApplicationVariant variant) {
		this.variant = variant
		group = TaskManager.INSTALL_GROUP
		description = "Installs the APK for ${variant.description}, and then runs the main launcher activity."
		onlyIf { variant }
	}

	@Override
	protected void exec() {
		def activityClass = getMainActivity(variant.outputs[0].processManifest.manifestOutputFile)
		// doesn't work: commandLine "${android.adbExe}", 'shell', 'am', 'start', '-a', 'android.intent.action.MAIN', '-c', 'android.intent.category.LAUNCHER', "${variant.applicationId}"
		commandLine project.android.adbExe, 'shell', 'am', 'start', '-n', "${variant.applicationId}/${activityClass}"
		// or commandLine android.adbExe, 'shell', 'monkey', '-p', "${variant.applicationId}", '1'
		super.exec()
	}

	static String getMainActivity(File file) {
		GPathResult xmlRoot = new XmlSlurper().parse(file)
		GPathResult launcherActivity = xmlRoot['application']['activity'].find(this.&isAppLauncher) as GPathResult
		return launcherActivity['@android:name']
	}

	static boolean isAppLauncher(activity) {
		return activity.'intent-filter'.find { intentFilter ->
			def isMain = intentFilter.action.find {
				it.'@android:name'.text() == 'android.intent.action.MAIN'
			}
			def isLauncher = intentFilter.category.find {
				it.'@android:name'.text() == 'android.intent.category.LAUNCHER'
			}
			return isMain && isLauncher
		}
	}

	// Unchecked warning without these, so let's just put it here
	@Override Map<String, Object> getEnvironment() {
		return super.getEnvironment()
	}
	@Override List<String> getArgs() {
		return super.getArgs()
	}
	@Override List<String> getCommandLine() {
		return super.getCommandLine()
	}
}
