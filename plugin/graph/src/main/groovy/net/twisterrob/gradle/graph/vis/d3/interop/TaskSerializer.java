package net.twisterrob.gradle.graph.vis.d3.interop;

import java.lang.reflect.Type;

import org.gradle.api.Task;

import com.google.gson.*;

public class TaskSerializer implements JsonSerializer<Task> {
	@Override public JsonElement serialize(Task task, Type type, JsonSerializationContext context) {
		return new JsonPrimitive(getKey(task));
	}

	public static String getKey(Task task) {
		return task.getPath();
	}
}
