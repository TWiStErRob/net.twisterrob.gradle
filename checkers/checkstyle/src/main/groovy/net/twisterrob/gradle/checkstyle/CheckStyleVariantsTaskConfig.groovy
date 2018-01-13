package net.twisterrob.gradle.checkstyle

import com.android.build.gradle.api.BaseVariant

class CheckStyleVariantsTaskConfig extends CheckStyleTask.TaskConfig {

	private final Collection<? extends BaseVariant> variants

	CheckStyleVariantsTaskConfig(Collection<? extends BaseVariant> variants) {
		this.variants = variants
	}

	@Override
	void execute(CheckStyleTask task) {
		task.description = "Run checkstyle batched on variants: ${variants*.name.join(', ')}"
		task.checkTargetName = variants*.name.join('+')
		setupConfigLocations(task)
		setupSources(task, variants)
		setupReports(task)
	}
}
