package net.twisterrob.gradle.internal.android

import com.android.build.gradle.internal.scope.TaskContainer
import com.android.build.gradle.internal.variant.BaseVariantData

/**
 * Return visibility of [BaseVariantData.taskContainer] changed from public to protected in AGP 7.4.
 * This means that running on >=7.4 would result in
 * > java.lang.IllegalAccessError: class AndroidHelpers_taskContainerCompat41xKt tried to access
 * > protected method BaseVariantData.getTaskContainer()LMutableTaskContainer;
 */
val BaseVariantData.taskContainerCompat74x: TaskContainer
	get() = BaseVariantData::class.java
		.getDeclaredField("taskContainer")
		.get(this)
			as TaskContainer
