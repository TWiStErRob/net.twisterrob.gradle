package net.twisterrob.gradle.graph.vis;

import java.io.*;
import java.util.Properties;

import org.gradle.cache.PersistentCache;
import org.gradle.internal.Factory;

import com.sun.javafx.beans.annotations.NonNull;

public abstract class VisualizerSettings<Settings> implements Closeable {
	private final PersistentCache cache;

	protected VisualizerSettings(PersistentCache cache) {
		this.cache = cache;
	}

	public Settings getSettings() {
		return cache.useCache("load properties", new Factory<Settings>() {
			@Override public Settings create() {
				File propsFile = new File(cache.getBaseDir(), getSettingsFileName());
				Properties props = new Properties();
				try {
					props.load(new FileReader(propsFile));
					return readSettings(props);
				} catch (FileNotFoundException ex) {
					return readSettings(new Properties()); // first startup
				} catch (IOException ex) {
					throw new IllegalStateException("Cannot read settings from " + propsFile, ex);
				}
			}
		});
	}

	protected abstract Settings readSettings(@NonNull Properties props);

	public void setSettings(Settings settings) {
		final Properties props = writeSettings(settings);
		cache.useCache("save properties", new Runnable() {
			@Override public void run() {
				File propsFile = new File(cache.getBaseDir(), getSettingsFileName());
				try {
					props.store(new FileWriter(propsFile), null);
				} catch (IOException e) {
					throw new IllegalStateException("Cannot save settings to " + propsFile, e);
				}
			}
		});
	}
	protected abstract Properties writeSettings(Settings settings);

	protected String getSettingsFileName() {
		return getClass().getName() + ".properties";
	}

	public void close() {
		cache.close();
	}
}
