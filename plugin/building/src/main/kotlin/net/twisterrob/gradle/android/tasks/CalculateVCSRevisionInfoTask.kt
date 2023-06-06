package net.twisterrob.gradle.android.tasks

import net.twisterrob.gradle.android.addBuildConfigField
import net.twisterrob.gradle.android.intermediateRegularFile
import net.twisterrob.gradle.internal.safeWriteText
import net.twisterrob.gradle.vcs.VCSExtension
import net.twisterrob.gradle.vcs.VCSPluginExtension
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.getByType

@CacheableTask
abstract class CalculateVCSRevisionInfoTask : DefaultTask() {

	@get:InputFiles
	@get:PathSensitive(PathSensitivity.RELATIVE)
	abstract val trackingFiles: ConfigurableFileCollection

	@get:Input
	abstract val revision: Property<String>

	@get:Input
	abstract val revisionNumber: Property<Int>

	@get:OutputFile
	abstract val revisionFile: RegularFileProperty

	@get:OutputFile
	abstract val revisionNumberFile: RegularFileProperty

	@get:Internal
	internal abstract val vcs: Property<VCSExtension>

	init {
		run { // === @Suppress("LeakingThis"), because .run { } is not detected.
			vcs.convention(project.provider { project.extensions.getByType<VCSPluginExtension>().current })
			// Delay input resolution to when the inputs are resolved for the task.
			trackingFiles.setFrom(vcs.map { it.files(project) })
			revision.convention(vcs.map(VCSExtension::revision))
			revisionNumber.convention(vcs.map(VCSExtension::revisionNumber))
			revisionFile.convention(project.intermediateRegularFile("buildConfigDecorations/revision.txt"))
			revisionNumberFile.convention(project.intermediateRegularFile("buildConfigDecorations/revisionNumber.txt"))
		}
	}

	@TaskAction
	fun writeVCS() {
		revisionFile.safeWriteText(revision.get())
		revisionNumberFile.safeWriteText(revisionNumber.get().toString())
	}

	companion object {

		internal fun TaskProvider<CalculateVCSRevisionInfoTask>.addBuildConfigFields(project: Project) {
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
