package net.twisterrob.gradle.internal.android

import com.android.build.gradle.internal.scope.MutableTaskContainer
import com.android.build.gradle.internal.scope.TaskContainer
import com.android.build.gradle.internal.variant.BaseVariantData

/**
 * Return type changed from [TaskContainer] interface to [MutableTaskContainer] class in AGP 4.1.
 * This means that compiling with >=4.1 would result in an
 * `INVOKEVIRTUAL BaseVariantData.getTaskContainer()LMutableTaskContainer;` instruction,
 * which fails on <4.1 with a [NoSuchMethodError] because of a differing method type.
 */
val BaseVariantData.taskContainerCompat40x: TaskContainer
	get() = this.taskContainer
