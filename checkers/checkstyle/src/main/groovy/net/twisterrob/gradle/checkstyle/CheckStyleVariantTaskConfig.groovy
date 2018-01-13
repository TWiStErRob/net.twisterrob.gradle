package net.twisterrob.gradle.checkstyle

import com.android.build.gradle.api.BaseVariant

class CheckStyleVariantTaskConfig extends CheckStyleTask.TaskConfig {

	private final BaseVariant variant

	CheckStyleVariantTaskConfig(BaseVariant variant) {
		this.variant = variant
	}

	@Override
	void execute(CheckStyleTask task) {
		task.description = "Run checkstyle on ${variant.name} variant"
		task.checkTargetName = variant.name
		setupConfigLocations(task)
		setupSources(task, [ variant ])
		setupReports(task, variant.name)
	}
}
