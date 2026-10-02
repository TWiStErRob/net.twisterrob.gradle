package net.twisterrob.gradle.android

import com.android.build.api.variant.AndroidTest
import net.twisterrob.gradle.internal.android.setOutputFileName84x
import org.gradle.api.Project
import org.gradle.api.provider.Provider

fun AndroidTest.setOutputFileName(apkName: Provider<String>, project: Project, variant: String) {
	this.setOutputFileName84x(apkName, project, variant)
}
