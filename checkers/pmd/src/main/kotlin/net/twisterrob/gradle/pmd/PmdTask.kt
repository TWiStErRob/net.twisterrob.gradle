package net.twisterrob.gradle.pmd

import net.twisterrob.gradle.common.TargetChecker
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.quality.Pmd
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input

@CacheableTask
@Suppress("detekt.UnnecessaryAbstractClass") // Gradle convention.
abstract class PmdTask : Pmd(), TargetChecker {

	@Suppress("detekt.LateinitUsage")
	@Input
	override lateinit var checkTargetName: String

	init {
		group = JavaBasePlugin.VERIFICATION_GROUP
		classpath = project.files()
		setupProperties()
	}

	private fun setupProperties() {
		// TODO expose similar properties to CS, for <rule message and path substitution
	}
}
