package net.twisterrob.gradle.checkstyle

import com.android.build.gradle.api.BaseVariant
import net.twisterrob.gradle.common.AndroidVariantApplier
import net.twisterrob.gradle.common.BaseExposedPlugin
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project

class CheckStylePlugin extends BaseExposedPlugin {

	@Override
	void apply(Project target) {
		super.apply(target)
		// level of indirection with base is to prevent loading classes in project not having Android
		project.plugins.withId('com.android.base') {
			new AndroidVariantApplier(project)
					.apply({DomainObjectSet<BaseVariant> variants ->
				new CheckStyleTaskCreator(project).applyTo(variants)
			})
		}
	}
}
