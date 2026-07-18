package net.twisterrob.gradle.android

import com.android.build.api.artifact.Artifacts
import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.artifact.impl.ArtifactsImpl
import net.twisterrob.gradle.common.AGPVersions
import net.twisterrob.gradle.internal.android.lintXmlReport81x
import net.twisterrob.gradle.internal.android.unwrapCast
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider

@Suppress("UnstableApiUsage")
val Artifacts.lintXmlReport: Provider<RegularFile>
	get() = when {
		AGPVersions.v92x <= AGPVersions.CLASSPATH -> this.get(SingleArtifact.LINT_XML_REPORT)
		AGPVersions.v81x <= AGPVersions.CLASSPATH -> this.unwrapCast<ArtifactsImpl>().lintXmlReport81x
		else -> AGPVersions.olderThan81NotSupported(AGPVersions.CLASSPATH)
	}
