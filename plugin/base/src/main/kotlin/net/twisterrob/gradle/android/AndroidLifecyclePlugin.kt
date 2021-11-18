package net.twisterrob.gradle.android

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.AndroidBasePlugin
import net.twisterrob.gradle.base.BasePlugin
import net.twisterrob.gradle.kotlin.dsl.extensions
import org.gradle.api.Project
import org.gradle.api.plugins.PluginInstantiationException
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.withType

@Suppress("MemberVisibilityCanBePrivate")
open class AndroidLifecycleExtension {

	companion object {

		internal const val NAME: String = "lifecycle"
	}

	val beforeAndroidEvaluateActions: MutableList<() -> Unit> = mutableListOf()
}

fun Project.beforeAndroidTasksCreated(block: () -> Unit) {
	this.lifecycle.beforeAndroidEvaluateActions.add(block)
}

private val Project.android: BaseExtension
	get() {
		if (!this.plugins.hasPlugin("com.android.base")) {
			throw PluginInstantiationException("Cannot use this before the Android plugins are applied.")
		}
		return this.extensions["android"] as BaseExtension
	}

private val Project.lifecycle: AndroidLifecycleExtension
	get() = this.android.extensions.getByName<AndroidLifecycleExtension>(AndroidLifecycleExtension.NAME)

class AndroidLifecyclePlugin : BasePlugin() {

	/**
	 * There's a tricky execution order required for this plugin to work:
	 *  * It has to be applied before any Android plugin.
	 *  * It has to hook into the right lifecycle to make sure data is propagated where it's needed.
	 *  * Default [version] extension info has to be initialized before [android] DSL is accessed from `build.gradle`.
	 *  * [version] extension has to be ready by the time it is used in [configure].
	 *
	 * 1. The [DefaultProductFlavor.getVersionName] is used by the Android plugin to propagate the version information.
	 * 2. This happens deep inside [com.android.build.gradle.BasePlugin.createAndroidTasks] (since 3.?, last check 3.2-beta05):
	 *  * [com.android.build.gradle.internal.VariantManager.createAndroidTasks]
	 *  * [com.android.build.gradle.internal.VariantManager.populateVariantDataList]
	 *  * [com.android.build.gradle.internal.VariantManager.createVariantDataForProductFlavors]
	 *  * [com.android.build.gradle.internal.VariantManager.createVariantDataForProductFlavorsAndVariantType]
	 *  * [com.android.build.gradle.internal.VariantManager.createVariantDataForVariantType]
	 *  * [com.android.build.gradle.internal.core.GradleVariantConfiguration.VariantConfigurationBuilder.create]
	 *  * [com.android.build.gradle.internal.core.GradleVariantConfiguration] constructor
	 *  * [com.android.build.gradle.internal.core.VariantConfiguration] constructor
	 *  * that merges in DefaultConfig
	 *  * in [com.android.builder.core.DefaultProductFlavor._initWith]: `this.mVersionName = thatProductFlavor.versionName`
	 * 3. So the `versionName` has to be set before the tasks are created in `afterEvaluate`.
	 */
	override fun apply(target: Project) {
		super.apply(target)
		// Order of execution denoted with /*[C#]*/ for Configuration Phase, and /*[A#]*/ for afterEvaluate
		// This method body is /*[C0]*/
		if (project.plugins.hasPlugin("com.android.base")) {
			throw PluginInstantiationException("This plugin must be applied before the android plugins.")
		}
		// just to make sure we're in the right module (see lazy initializer of android)
		project.afterEvaluate { project.android }
		project.plugins.withType<com.android.build.gradle.internal.plugins.BasePlugin<*, *, *>> {
			project.android.extensions.create<AndroidLifecycleExtension>(AndroidLifecycleExtension.NAME)
		}
		// when we detect that an Android plugin is going to be applied
		project.plugins.withType<AndroidBasePlugin> {
			// enqueue afterEvaluate, so it runs before BasePlugin.createAndroidTasks /*[A5]*/
			// see BasePlugin.createTasks /*[C2]*/ as to how createAndroidTasks is called.
			// Enables using project.beforeAndroidEvaluate { ... }
			project.afterEvaluate {
				project.lifecycle.beforeAndroidEvaluateActions.forEach { it() }
			}
		}
	}
}
