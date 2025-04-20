package net.twisterrob.gradle.android

import com.android.build.api.variant.AndroidTest
import net.twisterrob.gradle.common.AGPVersions
import net.twisterrob.gradle.internal.android.setOutputFileName81x
import net.twisterrob.gradle.internal.android.setOutputFileName84x
import org.gradle.api.Project
import org.gradle.api.provider.Provider

fun AndroidTest.setOutputFileName(apkName: Provider<String>, project: Project, variant: String) {
	when {
		AGPVersions.v84x <= AGPVersions.CLASSPATH -> this.setOutputFileName84x(apkName, project, variant)
		AGPVersions.v81x <= AGPVersions.CLASSPATH -> this.setOutputFileName81x(apkName, project, variant)
		else -> AGPVersions.olderThan7NotSupported(AGPVersions.CLASSPATH)
	}
}
