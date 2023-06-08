package net.twisterrob.gradle.graph.vis.d3.interop

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import net.twisterrob.gradle.graph.tasks.TaskData
import java.lang.reflect.Type

class TaskDataSerializer : JsonSerializer<TaskData> {

	override fun serialize(data: TaskData, type: Type, context: JsonSerializationContext): JsonElement =
		JsonObject().apply {
			add("label", context.serialize(data.task))
			add("type", context.serialize(data.type))
			add("state", context.serialize(data.state))
			add("deps", data.depsDirect.mapTo { context.serialize(it.task) })
		}
}

inline fun <T> Iterable<T>.mapTo(target: JsonArray = JsonArray(), transform: (T) -> JsonElement): JsonArray {
	for (item in this) {
		target.add(transform(item))
	}
	return target
}
