package net.twisterrob.gradle.graph.vis.d3.interop

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import org.gradle.api.Task
import java.lang.reflect.Type

class TaskSerializer : JsonSerializer<Task> {

	override fun serialize(task: Task, type: Type, context: JsonSerializationContext): JsonElement =
		JsonPrimitive(getKey(task))

	companion object {

		fun getKey(task: Task): String =
			task.path
	}
}
