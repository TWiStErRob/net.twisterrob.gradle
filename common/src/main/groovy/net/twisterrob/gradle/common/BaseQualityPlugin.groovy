package net.twisterrob.gradle.common

import com.android.build.gradle.api.BaseVariant
import groovy.transform.CompileDynamic
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project

class BaseQualityPlugin extends BaseExposedPlugin {

	final Class<?> taskCreator

	BaseQualityPlugin(Class<?> taskCreator) {
		this.taskCreator = taskCreator
	}

	@CompileDynamic // TODO extract applyTo to interface
	@Override
	void apply(Project target) {
		super.apply(target)
		// level of indirection with base is to prevent loading classes in project not having Android
		project.plugins.withId('com.android.base') {
			new AndroidVariantApplier(project)
					.apply({DomainObjectSet<BaseVariant> variants ->
				taskCreator.newInstance(project).applyTo(variants)
			})
		}
	}
}
