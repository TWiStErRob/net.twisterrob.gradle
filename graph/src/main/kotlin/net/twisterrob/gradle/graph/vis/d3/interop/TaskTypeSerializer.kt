package net.twisterrob.gradle.graph.vis.d3.interop

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import net.twisterrob.gradle.graph.tasks.TaskType
import java.lang.reflect.Type
import java.util.EnumMap

class TaskTypeSerializer : JsonSerializer<TaskType> {

	override fun serialize(taskType: TaskType, type: Type, context: JsonSerializationContext): JsonElement? =
		getType(taskType)?.let { JsonPrimitive(it) }

	companion object {

		private val MAPPING: Map<TaskType, String?> = EnumMap<TaskType, String>(TaskType::class.java).apply {
			this[TaskType.Unknown] = "unknown"
			this[TaskType.Normal] = null
			this[TaskType.Requested] = "requested"
			this[TaskType.Excluded] = "excluded"
			check(this.keys.size == TaskType.values().size)
		}

		fun getType(type: TaskType): String? =
			MAPPING.getValue(type)
	}
}
