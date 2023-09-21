package net.twisterrob.gradle.pmd

import net.twisterrob.gradle.common.ALL_VARIANTS_NAME
import net.twisterrob.gradle.common.TargetChecker
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.quality.Pmd
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input

@CacheableTask
abstract class PmdTask : Pmd(), TargetChecker {

	@Input
	override var checkTargetName: String = ALL_VARIANTS_NAME

	init {
		group = JavaBasePlugin.VERIFICATION_GROUP
		classpath = project.files()
		setupProperties()
	}

	private fun setupProperties() {
		// TODO expose similar properties to CS, for <rule message and path substitution
	}
}
