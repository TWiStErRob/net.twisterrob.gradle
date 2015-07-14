package net.twisterrob.gradle.graph.javafx.interop;

import java.lang.reflect.Type;

import com.google.gson.*;

import net.twisterrob.gradle.graph.TaskData;

public class TaskDataSerializer implements JsonSerializer<TaskData> {
	@Override public JsonElement serialize(TaskData data, Type type, JsonSerializationContext context) {
		JsonObject obj = new JsonObject();
		obj.add("label", new JsonPrimitive(data.getTask().getPath()));
		obj.add("type", context.serialize(data.getType()));
		obj.add("state", context.serialize(data.getState()));

		JsonArray deps = new JsonArray();
		for (TaskData dep : data.getDepsDirect()) {
			deps.add(context.serialize(dep.getTask()));
		}
		obj.add("deps", deps);

		return obj;
	}
}
