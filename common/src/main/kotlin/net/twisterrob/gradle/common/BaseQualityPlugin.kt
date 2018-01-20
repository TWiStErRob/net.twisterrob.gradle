package net.twisterrob.gradle.common

import com.android.build.gradle.api.BaseVariant
import org.gradle.api.Action
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project

open class BaseQualityPlugin(private val taskCreator: Class<out VariantTaskCreator<*>>) : BaseExposedPlugin() {

	override fun apply(target: Project) {
		super.apply(target)
		// level of indirection with base is to prevent loading classes in project not having Android
		project.plugins.withId("com.android.base") {
			AndroidVariantApplier(project).apply(Action { variants: DomainObjectSet<out BaseVariant> ->
				val newInstance = taskCreator.getDeclaredConstructor(Project::class.java).newInstance(project)
				newInstance.applyTo(variants)
			})
		}
	}
}
