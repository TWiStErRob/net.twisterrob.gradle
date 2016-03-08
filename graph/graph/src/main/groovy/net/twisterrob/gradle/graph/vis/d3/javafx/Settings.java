package net.twisterrob.gradle.graph.vis.d3.javafx;

import java.util.Properties;

import org.gradle.cache.PersistentCache;

import javafx.stage.Window;

import net.twisterrob.gradle.graph.vis.VisualizerSettings;

class Settings extends VisualizerSettings<Settings.WindowLocation> {
	Settings(PersistentCache cache) {
		super(cache);
	}

	@Override protected WindowLocation readSettings(Properties props) {
		WindowLocation location = new WindowLocation();
		location.x = parse(props.getProperty("x"), location.x);
		location.y = parse(props.getProperty("y"), location.y);
		location.width = parse(props.getProperty("width"), location.width);
		location.height = parse(props.getProperty("height"), location.height);
		return location;
	}

	@Override protected Properties writeSettings(WindowLocation location) {
		Properties props = new Properties();
		props.put("x", String.valueOf(location.x));
		props.put("y", String.valueOf(location.y));
		props.put("width", String.valueOf(location.width));
		props.put("height", String.valueOf(location.height));
		return props;
	}

	@Override protected WindowLocation createDefault() {
		return new WindowLocation();
	}
	private static double parse(String value, double defaultValue) {
		try {
			return Double.parseDouble(value);
		} catch (NullPointerException | NumberFormatException ex) {
			// ignore
		}
		return defaultValue;
	}

	static class WindowLocation {
		double x;
		double y;
		double width;
		double height;

		void applyTo(Window window) {
			window.setWidth(width);
			window.setHeight(height);
			window.setX(x);
			window.setY(y);
		}

		WindowLocation() {
			this.x = 0;
			this.y = 0;
			this.width = 800;
			this.height = 600;
		}

		WindowLocation(Window window) {
			this.x = window.getX();
			this.y = window.getY();
			this.width = window.getWidth();
			this.height = window.getHeight();
		}
	}
}
