package net.twisterrob.gradle.android.tasks

import net.twisterrob.gradle.android.addBuildConfigField
import net.twisterrob.gradle.android.intermediateRegularFile
import net.twisterrob.gradle.common.AGPVersions
import net.twisterrob.gradle.vcs.VCSPluginExtension
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.getByType

abstract class CalculateVCSRevisionInfoTask : DefaultTask() {

	@get:OutputFile
	val revisionFile: RegularFileProperty =
		intermediateRegularFile("buildConfigDecorations/revision.txt")

	@get:OutputFile
	val revisionNumberFile: RegularFileProperty =
		intermediateRegularFile("buildConfigDecorations/revisionNumber.txt")

	init {
		inputs.files(project.provider { vcs.current.files(project) })
	}

	private val vcs: VCSPluginExtension
		get() = project.extensions.getByType()

	@TaskAction
	fun writeVCS() {
		revisionFile.writeText(vcs.current.revision)
		revisionNumberFile.writeText(vcs.current.revisionNumber.toString())
	}

	companion object {

		internal fun TaskProvider<CalculateVCSRevisionInfoTask>.addBuildConfigFields(project: Project) {
			if (AGPVersions.CLASSPATH < AGPVersions.v41x) get().writeVCS()

			val revisionField = this.flatMap { it.revisionFile }.map {
				""""${it.asFile.readText()}""""
			}
			project.addBuildConfigField("REVISION", "String", revisionField)

			val revisionNumberField = this.flatMap { it.revisionNumberFile }.map {
				it.asFile.readText().toInt()
			}
			project.addBuildConfigField("REVISION_NUMBER", "int", revisionNumberField)
		}
	}
}
