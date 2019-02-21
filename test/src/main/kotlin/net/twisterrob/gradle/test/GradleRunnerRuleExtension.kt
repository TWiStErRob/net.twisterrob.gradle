package net.twisterrob.gradle.test

import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestInstancePostProcessor

open class GradleRunnerRuleExtension : TestInstancePostProcessor, BeforeEachCallback, AfterEachCallback {

	private val rule = GradleRunnerRule()

	override fun postProcessTestInstance(testInstance: Any, context: ExtensionContext) {
		testInstance
			.javaClass
			.declaredFields
			.filter { it.type === GradleRunnerRule::class.java }
			.onEach { it.isAccessible = true }
			.forEach { field -> field.set(testInstance, rule) }
	}

	override fun beforeEach(context: ExtensionContext) {
		rule.before()
	}

	override fun afterEach(context: ExtensionContext) {
		rule.after(!context.executionException.isPresent)
	}
}
