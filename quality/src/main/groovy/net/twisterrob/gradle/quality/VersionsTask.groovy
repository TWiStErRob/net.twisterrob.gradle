package net.twisterrob.gradle.quality

import org.gradle.api.DefaultTask
import org.gradle.api.plugins.quality.CheckstyleExtension
import org.gradle.api.plugins.quality.CodeQualityExtension
import org.gradle.api.plugins.quality.FindBugsExtension
import org.gradle.api.plugins.quality.PmdExtension
import org.gradle.api.tasks.TaskAction

class VersionsTask extends DefaultTask {

	VersionsTask() {
		super()
		outputs.upToDateWhen {false}
	}

	@TaskAction printVersions() {
		println """
			Gradle version: ${project.gradle.gradleVersion}
			Checkstyle version: ${getVersion('checkstyle', CheckstyleExtension)}
			PMD version: ${getVersion('pmd', PmdExtension)}
			FindBugs version: ${getVersion('findbugs', FindBugsExtension)}
		""".stripIndent()
	}

	private String getVersion(String pluginName, Class<? extends CodeQualityExtension> type) {
		project.extensions.findByType(type)?.toolVersion?: "'${pluginName}' plugin not applied"
	}
}
