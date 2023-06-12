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
				this[TaskResult.Executing] = "executing"
				this[TaskResult.NoWork] = "nowork"
				this[TaskResult.Skipped] = "skipped"
				this[TaskResult.UpToDate] = "uptodate"
				this[TaskResult.NoSource] = "nosource"
				this[TaskResult.FromCache] = "fromcache"
				this[TaskResult.Failure] = "failure"
				this[TaskResult.Completed] = "success"
				check(this.keys.size == TaskResult.values().size)
			}

		fun getState(result: TaskResult): String =
			MAPPING.getValue(result)
	}
}
