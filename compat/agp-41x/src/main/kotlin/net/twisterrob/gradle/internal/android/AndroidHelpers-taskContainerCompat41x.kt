package net.twisterrob.gradle.internal.android

import com.android.build.gradle.internal.scope.TaskContainer
import com.android.build.gradle.internal.variant.BaseVariantData

val BaseVariantData.taskContainerCompat41x: TaskContainer
	get() = this.taskContainer
