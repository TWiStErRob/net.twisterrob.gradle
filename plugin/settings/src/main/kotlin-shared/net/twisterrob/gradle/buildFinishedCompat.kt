@file:JvmMultifileClass
@file:JvmName("GradleUtils")

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
import org.gradle.util.GradleVersion
import java.util.function.Consumer

/**
 * This only covers some very basic use cases.
 * If you need more, use the Gradle 8.1+ API directly and define your own [FlowAction] and [FlowParameters].
 *
 * Note: Not using an `Action<Throwable?>` because Kotlin DSL would make the parameter a receiver.
 */
@Incubating
@Suppress("UnstableApiUsage")
fun Gradle.buildFinishedCompat(action: Consumer<Throwable?>) {
	if (GradleVersion.version("8.1") <= GradleVersion.current()) {
		serviceOf<FlowScope>().always(ExecuteAction::class.java) {
			parameters.action.set(action)
			val buildResult = serviceOf<FlowProviders>().buildWorkResult
			parameters.failure.set(buildResult.map { workResult -> workResult.failure.orElse(null) })
		}
	} else {
		@Suppress("DEPRECATION")
		this.buildFinished {
			action.accept(this.failure)
		}
	}
}

@Suppress("UnstableApiUsage")
private class ExecuteAction : FlowAction<ExecuteAction.Parameters> {
	interface Parameters : FlowParameters {
		@get:Input
		val action: Property<Consumer<Throwable?>>

		@get:Input
		val failure: Property<Throwable>
	}

	override fun execute(parameters: Parameters) {
		@Suppress("TYPE_MISMATCH") // It's declared nullable, it is nullable.
		parameters.action.get().accept(parameters.failure.orNull)
	}
}
