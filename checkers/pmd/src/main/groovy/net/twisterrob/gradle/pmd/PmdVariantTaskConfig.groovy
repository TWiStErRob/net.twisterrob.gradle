package net.twisterrob.gradle.pmd

import com.android.build.gradle.api.BaseVariant

class PmdVariantTaskConfig extends PmdTask.TaskConfig {

	private final BaseVariant variant

	PmdVariantTaskConfig(BaseVariant variant) {
		this.variant = variant
	}

	@Override
	void execute(PmdTask task) {
		task.description = "Run pmd on ${variant.name} variant"
		task.checkTargetName = variant.name
		setupConfigLocations(task)
		setupSources(task, [ variant ])
		setupReports(task, variant.name)
	}
}
