package net.twisterrob.gradle

import org.gradle.api.Incubating
import org.gradle.api.flow.FlowAction
import org.gradle.api.flow.FlowParameters
import org.gradle.api.flow.FlowProviders
import org.gradle.api.flow.FlowScope
import org.gradle.api.invocation.Gradle
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.support.serviceOf

/**
 * @since Gradle 8.1
 */
@Incubating
@Suppress("UnstableApiUsage")
fun Gradle.buildFlowFinished(action: (Throwable?) -> Unit) {
	serviceOf<FlowScope>().always(ExecuteAction::class.java) {
		parameters.action.set(action)
		val buildResult = serviceOf<FlowProviders>().buildWorkResult
		parameters.failure.set(buildResult.map { it.failure.orElse(null) })
	}
}

@Suppress("UnstableApiUsage")
private class ExecuteAction : FlowAction<ExecuteAction.Parameters> {
	interface Parameters : FlowParameters {
		@get:Input
		val action: Property<(Throwable?) -> Unit>

		@get:Input
		val failure: Property<Throwable>
	}

	override fun execute(parameters: Parameters) {
		parameters.action.get().invoke(parameters.failure.orNull)
	}
}
