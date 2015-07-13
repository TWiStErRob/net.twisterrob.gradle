package net.twisterrob.gradle.graph.javafx.interop;

import java.util.Arrays;

import javafx.application.Platform;
import javafx.scene.web.WebEngine;
import netscape.javascript.JSObject;

public class JavaScriptBridge {
	private final JSObject JSON;
	private final JSObject model;
	public JavaScriptBridge(WebEngine engine) {
		this.JSON = (JSObject)engine.executeScript("JSON");
		this.model = (JSObject)engine.executeScript("model");
	}
	private void modelCall(final String methodName, final Object... args) {
		final String argsStr = Arrays.toString(args);
		final String argsShort = argsStr.length() < 50? argsStr
				: argsStr.substring(0, 50).replaceAll("\\s+", " ") + "...";
		//message(methodName + "(" + argsShort + ")");
		Platform.runLater(new Runnable() {
			@Override public void run() {
				try {
					model.call(methodName, args);
				} catch (RuntimeException ex) {
					throw new RuntimeException("Failure " + methodName + "(" + argsStr + ")", ex);
				}
			}
		});
	}
	public void message(String message) {
		System.err.println(message);
	}

	@SuppressWarnings("unused")
	public void log(JSObject args) {
		for (int i = 0, len = (Integer)args.getMember("length"); i < len; i++) {
			Object arg = args.getSlot(i);
			System.err.print(JSON.call("stringify", new Object[] {arg}));
			if (i < len - 1) {
				System.err.print(", ");
			}
		}
		System.err.println();
	}

	public void init(String graph) {
		modelCall("init", graph);
	}
	public void update(String name, String result) {
		modelCall("update", name, result);
	}
}
