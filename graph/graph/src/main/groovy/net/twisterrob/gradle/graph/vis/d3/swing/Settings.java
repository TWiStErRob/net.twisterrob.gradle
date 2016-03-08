package net.twisterrob.gradle.graph.vis.d3.swing;

import java.awt.Component;
import java.util.Properties;

import org.gradle.cache.PersistentCache;

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

	private static int parse(String value, int defaultValue) {
		try {
			return Integer.parseInt(value);
		} catch (NullPointerException | NumberFormatException ex) {
			// ignore
		}
		return defaultValue;
	}

	static class WindowLocation {
		int x;
		int y;
		int width;
		int height;

		void applyTo(Component window) {
			window.setLocation(x, y);
			window.setSize(width, height);
		}

		WindowLocation() {
			this.x = 0;
			this.y = 0;
			this.width = 800;
			this.height = 600;
		}

		WindowLocation(Component window) {
			this.x = window.getLocation().x;
			this.y = window.getLocation().y;
			this.width = window.getSize().width;
			this.height = window.getSize().height;
		}
	}
}
