package net.twisterrob.gradle.common

import net.twisterrob.gradle.android.androidComponents
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.SourceTask

@Suppress("detekt.UnnecessaryAbstractClass") // Gradle convention.
abstract class BaseQualityPlugin(
	private val taskCreatorType: Class<out VariantTaskCreator<*>>,
	private val extensionName: String,
	private val extensionType: Class<out BaseQualityExtension<out SourceTask>>
) : BaseExposedPlugin() {

	override fun apply(target: Project) {
		super.apply(target)

		@Suppress("detekt.CastToNullableType") // This is a lazy creation, so findByName is very likely null.
		val quality = project.extensions.findByName("quality") as ExtensionAware?
			?: project.extensions.create("quality", FakeQualityExtension::class.java) as ExtensionAware
		quality.extensions.create(extensionName, extensionType)

		// level of indirection with base is to prevent loading classes in project not having Android
		project.plugins.withId("com.android.base") {
			val taskCreator = project.objects.newInstance(taskCreatorType)
			project.androidComponents(taskCreator::applyTo)
		}
		project.plugins.withId("org.gradle.java") {
			val taskCreator = taskCreatorType.newInstance(project)
			taskCreator.applyToJvm()
		}
	}
}

private inline fun <T, reified P1> Class<T>.newInstance(p1: P1?): T =
	this.getDeclaredConstructor(P1::class.java).newInstance(p1)

@Suppress("detekt.UnnecessaryAbstractClass") // Gradle convention.
private abstract class FakeQualityExtension
