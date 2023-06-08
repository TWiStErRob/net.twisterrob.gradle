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
			this[TaskType.unknown] = "unknown"
			this[TaskType.normal] = null
			this[TaskType.requested] = "requested"
			this[TaskType.excluded] = "excluded"
			assert(this.keys.size == TaskType.values().size)
		}

		fun getType(type: TaskType): String? =
			MAPPING.getValue(type)
	}
}
