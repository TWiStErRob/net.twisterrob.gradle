package net.twisterrob.gradle.internal.android

import com.android.build.api.artifact.impl.ArtifactsImpl
import com.android.build.gradle.internal.scope.InternalArtifactType
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider

val ArtifactsImpl.lintXmlReport81x: Provider<RegularFile>
	get() = this.get(InternalArtifactType.LINT_XML_REPORT)
