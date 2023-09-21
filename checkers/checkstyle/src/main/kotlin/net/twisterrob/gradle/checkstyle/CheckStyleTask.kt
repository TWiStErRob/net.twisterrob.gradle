package net.twisterrob.gradle.checkstyle

import net.twisterrob.gradle.common.ALL_VARIANTS_NAME
import net.twisterrob.gradle.common.TargetChecker
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input

@CacheableTask
abstract class CheckStyleTask : Checkstyle(), TargetChecker {

	@Input
	override var checkTargetName: String = ALL_VARIANTS_NAME

	init {
		group = JavaBasePlugin.VERIFICATION_GROUP
		classpath = project.files()
		isShowViolations = false

		setupProperties()
	}

	private fun setupProperties() {
		// partially based on https://github.com/jshiell/checkstyle-idea#eclipse-cs-variable-support
		configProperties = configProperties.orEmpty() + mapOf(
			"basedir" to project.projectDir, // TODO or rootDir?
			"project_loc" to project.rootDir,
			"workspace_loc" to project.rootDir
			// Note: https://docs.gradle.org/current/userguide/upgrading_version_6.html#setting_the_config_loc_config_property_on_the_checkstyle_plugin_is_now_an_error
			//"config_loc" to configFile.parentFile // set via setConfigDir
			//"samedir" to /*use config_loc instead (until I figure out how to do doFirst properly)*/
		)
	}
}
