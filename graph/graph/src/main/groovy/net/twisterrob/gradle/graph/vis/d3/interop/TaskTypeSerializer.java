package net.twisterrob.gradle.graph.vis.d3.interop;

import java.lang.reflect.Type;
import java.util.EnumMap;

import com.google.gson.*;

import net.twisterrob.gradle.graph.tasks.TaskType;

public class TaskTypeSerializer implements JsonSerializer<TaskType> {
	@Override public JsonElement serialize(TaskType taskType, Type type, JsonSerializationContext context) {
		String typeString = getType(taskType);
		return typeString != null? new JsonPrimitive(typeString) : null;
	}

	private static final EnumMap<TaskType, String> MAPPING = new EnumMap<>(TaskType.class);

	static {
		MAPPING.put(TaskType.unknown, "unknown");
		MAPPING.put(TaskType.normal, null);
		MAPPING.put(TaskType.requested, "requested");
		MAPPING.put(TaskType.excluded, "excluded");
		assert MAPPING.keySet().size() == TaskType.values().length;
	}

	public static String getType(TaskType type) {
		return MAPPING.get(type);
	}
}
