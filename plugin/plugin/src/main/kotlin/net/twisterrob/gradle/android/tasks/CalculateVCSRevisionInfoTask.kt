package net.twisterrob.gradle.android.tasks

import net.twisterrob.gradle.android.asBuildConfigField
import net.twisterrob.gradle.android.intermediateRegularFile
import net.twisterrob.gradle.android.onVariantProperties
import net.twisterrob.gradle.vcs.VCSPluginExtension
import net.twisterrob.gradle.writeText
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.getByName

open class CalculateVCSRevisionInfoTask : DefaultTask() {

	@get:OutputFile
	val revisionFile: RegularFileProperty = intermediateRegularFile("buildConfigDecorations/revision.txt")

	@get:OutputFile
	val revisionNumberFile: RegularFileProperty = intermediateRegularFile("buildConfigDecorations/revisionNumber.txt")

	init {
		outputs.upToDateWhen { false }
	}

	@TaskAction
	fun writeVCS() {
		val vcs = project.extensions.getByName<VCSPluginExtension>(VCSPluginExtension.NAME)
		revisionFile.writeText(vcs.current.revision)
		revisionNumberFile.writeText(vcs.current.revisionNumber.toString())
	}

	companion object {

		fun TaskProvider<CalculateVCSRevisionInfoTask>.addBuildConfigFields(project: Project) {
			val revisionField = flatMap { it.revisionFile }.map {
				it.asBuildConfigField("String") { """"${it}"""" }
			}
			val revisionNumberField = flatMap { it.revisionNumberFile }.map {
				it.asBuildConfigField("int") { it.toInt() }
			}
			project.onVariantProperties {
				buildConfigFields.put("REVISION", revisionField)
				buildConfigFields.put("REVISION_NUMBER", revisionNumberField)
			}
		}
	}
}
