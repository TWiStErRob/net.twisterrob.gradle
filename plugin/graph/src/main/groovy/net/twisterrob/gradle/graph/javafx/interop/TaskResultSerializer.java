package net.twisterrob.gradle.graph.javafx.interop;

import java.lang.reflect.Type;
import java.util.EnumMap;

import com.google.gson.*;

import net.twisterrob.gradle.graph.TaskResult;

public class TaskResultSerializer implements JsonSerializer<TaskResult> {
	@Override public JsonElement serialize(TaskResult taskResult, Type type, JsonSerializationContext context) {
		String stateString = getState(taskResult);
		return stateString != null? new JsonPrimitive(stateString) : null;
	}

	private static final EnumMap<TaskResult, String> MAPPING = new EnumMap<>(TaskResult.class);

	static {
		MAPPING.put(TaskResult.executing, "executing");
		MAPPING.put(TaskResult.nowork, "nowork");
		MAPPING.put(TaskResult.skipped, "skipped");
		MAPPING.put(TaskResult.uptodate, "uptodate");
		MAPPING.put(TaskResult.failure, "failure");
		MAPPING.put(TaskResult.completed, "success");
		assert MAPPING.keySet().size() == TaskResult.values().length;
	}

	public static String getState(TaskResult result) {
		return MAPPING.get(result);
	}
}
