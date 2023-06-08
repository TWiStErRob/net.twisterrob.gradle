package net.twisterrob.gradle.graph.vis.d3.interop

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import net.twisterrob.gradle.graph.tasks.TaskResult
import java.lang.reflect.Type
import java.util.EnumMap

class TaskResultSerializer : JsonSerializer<TaskResult> {

	override fun serialize(taskResult: TaskResult, type: Type, context: JsonSerializationContext): JsonElement =
		JsonPrimitive(getState(taskResult))

	companion object {

		private val MAPPING: Map<TaskResult, String> =
			EnumMap<TaskResult, String>(TaskResult::class.java).apply {
				this[TaskResult.executing] = "executing"
				this[TaskResult.nowork] = "nowork"
				this[TaskResult.skipped] = "skipped"
				this[TaskResult.uptodate] = "uptodate"
				this[TaskResult.failure] = "failure"
				this[TaskResult.completed] = "success"
				assert(this.keys.size == TaskResult.values().size)
			}

		fun getState(result: TaskResult): String =
			MAPPING.getValue(result)
	}
}
