package net.twisterrob.gradle.common;

import javax.annotation.Nonnull;

import org.gradle.api.Project;

// TODEL remove when BaseExposedPlugin is Kotlin
public class BaseExposedPluginForKotlin extends BasePluginForKotlin {

	@Override
	public void apply(@Nonnull Project target) {
		super.apply(target);

		new BaseExposedPlugin().apply(target);
	}
}
