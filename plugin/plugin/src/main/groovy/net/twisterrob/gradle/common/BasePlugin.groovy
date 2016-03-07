package net.twisterrob.gradle.common

import org.apache.tools.ant.BuildException
import org.gradle.api.*
import org.slf4j.*

import java.text.SimpleDateFormat
import java.util.jar.Manifest

class BasePlugin implements Plugin<Project> {
	Logger LOG = LoggerFactory.getLogger(getClass())

	protected Project project

	@Override
	void apply(Project target) {
		LOG.debug "Applying to ${target}"
		project = target

		def match = (project.gradle.gradleVersion =~ /(\d+)\.(\d+).*/);
		if (!match.find() || match.group(1) != '2' || (match.group(2) as int) < 4) {
			File file = new File("gradle" + File.separator + "wrapper" + File.separator + "gradle-wrapper.properties");
			def required = "2.4+"
			throw new BuildException(
					"Gradle version ${required} is required; the current version is ${project.gradle.gradleVersion}."
							+ " Edit the distributionUrl in ${file.absolutePath}."
			)
		}
	}

	Date getBuiltDate() {
		URL res = getClass().getResource(getClass().getSimpleName() + ".class");
		JarURLConnection conn = (JarURLConnection)res.openConnection();
		Manifest mf = conn.getManifest();
		def date = mf.getMainAttributes().getValue("Built-Date");
		return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(date);
	}
}
