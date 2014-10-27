package net.twisterrob.gradle.android

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.*
import com.android.build.gradle.internal.BadPluginException
import net.twisterrob.gradle.common.BasePlugin
import net.twisterrob.gradle.vcs.VCSPluginExtension
import org.gradle.api.Project

class AndroidVersionExtension {
	/** Default versionCode pattern is MMMNNPPBBB (what fits into 2147483648) */
	boolean autoVersion = true
	String versionNameFormat = '%1$d.%2$d.%3$d#%4$d'
	File versionFile = new File('version.properties')
	int major = 0 // M 0..213
	int minor = 0 // N 0..99
	int minorMagnitude = 100
	int patch = 0 // P 0..99
	int patchMagnitude = 100
	int build = 0 // B 0..999
	int buildMagnitude = 1000

	void versionByVCS(vcs) {
		minorMagnitude = 10
		patchMagnitude = 10
		buildMagnitude = 100000
		build = vcs.revisionNumber
	}

	boolean renameAPK = true
	Closure<String> formatArtifactName = { Project project, ApkVariant variant, String baseName ->
		baseName = baseName.replace("${project.archivesBaseName}-", "") // strip project name, leave only variant
		"${variant.applicationId}@${variant.versionCode}-v${variant.versionName}+${baseName}"
	}
}

public class AndroidVersionPlugin extends BasePlugin {
	private AndroidVersionExtension version

	@Override
	void apply(Project target) {
		super.apply(target)

		if (!project.plugins.hasPlugin('com.android.application')) {
			throw new BadPluginException("Can only use versioning with Android applications")
		}
		AppExtension android = project.android
		version = android.defaultConfig.extensions.create('version', AndroidVersionExtension)
		version.versionFile = project.file(version.versionFile.name)

		VCSPluginExtension vcs = project.VCS
		if (vcs && vcs.current.available) {
			version.versionByVCS(vcs.current)
		}

		project.afterEvaluate {
			android.applicationVariants.all { variant ->
				if (version.autoVersion) {
					autoVersion(variant)
				}
				if (version.renameAPK) {
					appendVersionNameVersionCode(variant)
				}
			}
		}

		/*// Not sure if needed to overwrite by default
		if (version.autoVersion) {
			android.defaultConfig.with {
				versionName = calculateVersionName()
				versionCode = calculateVersionCode()
			}
		}*/
	}

	void appendVersionNameVersionCode(ApplicationVariant variant) {
		//noinspection GroovyAssignabilityCheck
		for (ApkVariantOutput output : variant.outputs) {
			updateOutput(variant, output.zipAlign)
			updateOutput(variant, output.packageApplication)
		}
	}

	private void updateOutput(ApplicationVariant variant, OutputFileTask task) {
		if (task) {
			File original = task.outputFile
			String name = original.name
			def artifact = name.endsWith(".apk") ? name.substring(0, name.length() - ".apk".length()) : name
			task.outputFile = new File(original.parent, version.formatArtifactName(project, variant, artifact) + ".apk")
		}
	}

	void autoVersion(BaseVariant variant) {
		variant.mergedFlavor.setVersionName(calculateVersionName())
		variant.mergedFlavor.setVersionCode(calculateVersionCode())
	}

	String calculateVersionName() {
		readFromFileIfNeeded()
		return String.format(version.versionNameFormat, version.major, version.minor, version.patch, version.build)
	}

	int calculateVersionCode() {
		readFromFileIfNeeded()
		return ((version.major * version.minorMagnitude + version.minor) * version.patchMagnitude + version.patch) *
				version.buildMagnitude + version.build
	}

	private void readFromFileIfNeeded() {
		if (version.versionFile) {
			def versionProps = readVersion(version.versionFile)
			if (versionProps.getProperty('major')) {
				version.major = versionProps.getProperty('major') as int
			}
			if (versionProps.getProperty('minor')) {
				version.minor = versionProps.getProperty('minor') as int
			}
			if (versionProps.getProperty('patch')) {
				version.patch = versionProps.getProperty('patch') as int
			}
			if (versionProps.getProperty('build')) {
				version.build = versionProps.getProperty('build') as int
			}
		}
	}

	static Properties readVersion(File file) {
		def version = new Properties()
		def stream
		try {
			stream = new FileInputStream(file)
			version.load(stream)
		} catch (FileNotFoundException ignore) {
		} finally {
			if (stream != null) {
				stream.close()
			}
		}
		return version
	}
}
