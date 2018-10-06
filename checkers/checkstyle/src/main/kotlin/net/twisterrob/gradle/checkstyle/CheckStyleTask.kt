package net.twisterrob.gradle.checkstyle

import net.twisterrob.gradle.common.ALL_VARIANTS_NAME
import net.twisterrob.gradle.common.TargetChecker
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.tasks.Input

open class CheckStyleTask : Checkstyle(), TargetChecker {

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
		configProperties = (configProperties ?: emptyMap()) + mapOf(
				"basedir" to project.projectDir, // TODO or rootDir?
				"project_loc" to project.rootDir,
				"workspace_loc" to project.rootDir
				//"config_loc" to configFile.parentFile // set via setConfigDir
				//"samedir" to /*use config_loc instead (until I figure out how to do doFirst properly)*/
		)
	}
}
