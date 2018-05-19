package net.twisterrob.gradle.common;

import javax.annotation.Nonnull;

import org.gradle.api.*;

// TODEL remove when BasePlugin is Kotlin
public class BasePluginForKotlin implements Plugin<Project> {

	protected Project project;

	@Override
	public void apply(@Nonnull Project target) {
		this.project = target;
		new BasePlugin().apply(target);
	}
}
