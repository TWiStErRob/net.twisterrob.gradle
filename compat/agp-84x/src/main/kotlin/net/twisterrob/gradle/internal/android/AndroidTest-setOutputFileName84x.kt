package net.twisterrob.gradle.internal.android

import com.android.build.api.component.impl.DeviceTestImpl
import com.android.build.api.variant.AndroidTest
import com.android.build.api.variant.MultiOutputHandler
import com.android.build.gradle.internal.scope.MutableTaskContainer
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import java.lang.reflect.Field

fun AndroidTest.setOutputFileName84x(apkName: Provider<String>, project: Project, variant: String) {
	project.afterEvaluate {
		(this as DeviceTestImpl).taskContainer.setTaskOutputFileName(apkName, variant)
	}
}

fun MutableTaskContainer.setTaskOutputFileName(apkName: Provider<String>, variant: String) {
	checkNotNull(packageAndroidTask) { "Missing package task for ${variant}'s androidTest." }
		.configure { packageTask ->
			packageTask.outputsHandler.get().singleOutputFileName = apkName
		}
}

private var MultiOutputHandler.singleOutputFileName: Provider<String>
	@Suppress("UNCHECKED_CAST")
	get() = singleOutputFileNameField.get(this) as Provider<String>
	set(value) {
		singleOutputFileNameField.set(this, value)
	}

/**
 * Cannot reference the real class, because it's internal, so use the instance instead.
 *
 * @see com.android.build.api.variant.impl.SingleOutputHandler.singleOutputFileName
 */
private val MultiOutputHandler.singleOutputFileNameField: Field
	get() = this::class.java
		.getDeclaredField("singleOutputFileName")
		.apply { isAccessible = true }
