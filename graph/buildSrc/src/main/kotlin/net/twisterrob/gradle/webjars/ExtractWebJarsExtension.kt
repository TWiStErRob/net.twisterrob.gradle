package net.twisterrob.gradle.webjars

import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.named

abstract class ExtractWebJarsExtension(
	private val project: Project,
) {
	fun extractInto(target: SourceDirectorySet) {
		target.srcDir(project.tasks.named("extractWebJars"))
	}

	fun extractIntoFirstJavaResourcesFolder() {
		val sourceSets = project.extensions.getByName<SourceSetContainer>("sourceSets")
		val firstResourceDir = sourceSets.named("main").map { it.resources.srcDirs.first() }
		extractIntoExistingFolder(project.layout.dir(firstResourceDir))
	}

	fun extractIntoExistingFolder(folder: Provider<Directory>) {
		val extractWebJars = project.tasks.named<ExtractWebJarsTask>("extractWebJars") {
			doNotTrackState("The output directory overlaps with an existing source folder.")
			cleanFirst.set(false)
			outputDirectory.set(folder)
		}
		project.tasks.named("processResources").configure { dependsOn(extractWebJars) }
	}
}
