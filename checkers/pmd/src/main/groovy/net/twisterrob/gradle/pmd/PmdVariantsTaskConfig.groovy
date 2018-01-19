package net.twisterrob.gradle.pmd

import com.android.build.gradle.api.BaseVariant
import net.twisterrob.gradle.common.Constants

class PmdVariantsTaskConfig extends PmdTask.TaskConfig {

	private final Collection<? extends BaseVariant> variants

	PmdVariantsTaskConfig(Collection<? extends BaseVariant> variants) {
		this.variants = variants
	}

	@Override
	void execute(PmdTask task) {
		task.description = "Run pmd batched on variants: ${variants*.name.join(', ')}"
		task.checkTargetName = Constants.ALL_VARIANTS_NAME
		setupConfigLocations(task)
		setupSources(task, variants)
		setupReports(task)
	}
}
