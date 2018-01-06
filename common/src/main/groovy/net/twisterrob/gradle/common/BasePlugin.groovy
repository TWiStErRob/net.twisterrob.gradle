package net.twisterrob.gradle.common

import org.apache.tools.ant.BuildException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class BasePlugin implements Plugin<Project> {

	Logger LOG = LoggerFactory.getLogger(getClass())

	protected Project project

	@Override
	void apply(Project target) {
		LOG.debug "Applying to ${target}"
		project = target

		def match = (project.gradle.gradleVersion =~ /(?<major>\d+)\.(?<minor>\d+).*/)
		if (!match.find() || !(match.group('major') == '4' && 1 <= (match.group('minor') as int))) {
			File file = new File("gradle" + File.separator + "wrapper" + File.separator + "gradle-wrapper.properties")
			def required = "4.1+"
			throw new BuildException(
					"Gradle version ${required} is required; the current version is ${project.gradle.gradleVersion}."
							+ " Edit the distributionUrl in ${file.absolutePath}."
			)
		}
	}
}
