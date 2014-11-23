package net.twisterrob.gradle.common

import org.gradle.api.*
import org.slf4j.*

class BasePlugin implements Plugin<Project> {
	Logger LOG = LoggerFactory.getLogger(getClass())

	protected Project project

	@Override
	void apply(Project target) {
		LOG.debug "Applying to ${target}"
		project = target
	}
}
