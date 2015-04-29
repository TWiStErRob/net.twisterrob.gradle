package net.twisterrob.gradle.common

import org.gradle.api.*
import org.slf4j.*

import java.text.SimpleDateFormat
import java.util.jar.*

class BasePlugin implements Plugin<Project> {
	Logger LOG = LoggerFactory.getLogger(getClass())

	protected Project project

	@Override
	void apply(Project target) {
		LOG.debug "Applying to ${target}"
		project = target
	}

	Date getBuiltDate() {
		URL res = getClass().getResource(getClass().getSimpleName() + ".class");
		JarURLConnection conn = (JarURLConnection)res.openConnection();
		Manifest mf = conn.getManifest();
		def date = mf.getMainAttributes().getValue("Built-Date");
		return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(date);
	}
}
