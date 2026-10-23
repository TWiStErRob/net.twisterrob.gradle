package net.twisterrob.gradle.pmd

import com.android.build.api.variant.Variant
import net.twisterrob.gradle.common.VariantTaskCreator
import org.gradle.api.Project
import java.io.File
import javax.inject.Inject

open class PmdTaskCreator @Inject constructor(
	project: Project,
) : VariantTaskCreator<PmdTask>(
	project,
	"pmd",
	"org.gradle.pmd",
	PmdTask::class.java,
	PmdExtension::class.java
) {

	override fun taskConfigurator(): VariantTaskCreator<PmdTask>.DefaultTaskConfig =
		object : VariantTaskCreator<PmdTask>.DefaultTaskConfig() {

			override fun setupConfigLocations(task: PmdTask) {
				task.ruleSets = emptyList() // default is java-basic
				val rootConfig = task.project.rootDir.resolve("config/pmd/pmd.xml")
				@Suppress("detekt.MaxChainedCallsOnSameLine")
				val subConfig = task.project.layout.projectDirectory.file("config/pmd/pmd.xml").asFile
				val config: File? = listOf(subConfig, rootConfig).firstOrNull { it.exists() }
				if (config != null) {
					task.ruleSetFiles += task.project.files(config)
				} else if (task.ruleSetFiles.files.isEmpty()) {
					task.doFirst("Warn about missing configuration files.") {
						task.logger.warn(
							"""
								|While auto-configuring ruleSetFiles for ${task}, there was no configuration found at:
								|	rootProject=${rootConfig}
								|	subProject=${subConfig}
								|	and there's no configuration location set in Gradle build files either.
							""".trimMargin()
						)
					}
				}

				// put configuration files on classpath of PMD so it's possible to reference own rulesets with relative path
				task.classpath = (task.classpath ?: task.project.files()) +
						task.project.files(task.ruleSetFiles.map { it.parentFile })
			}

			override fun setupSources(task: PmdTask, variant: Variant) {
				super.setupSources(task, variant)

				@Suppress("detekt.MaxChainedCallsOnSameLine")
				val buildPath = task.project.layout.buildDirectory.get().asFile.toPath()
				@Suppress("detekt.MaxChainedCallsOnSameLine")
				val projectPath = task.project.layout.projectDirectory.asFile.toPath()
				if (!buildPath.startsWith(projectPath)) {
					task.logger.warn(
						"""
							|Cannot set up ${task} source folders,
							|	because the build directory ${buildPath}
							|	needs to be inside the project directory ${projectPath}.
						""".trimMargin().replace("""\r?\n\t*""".toRegex(), " ")
					)
					return
				}

				task.source(variant.sources.res?.all)
				task.source(variant.sources.manifests.all)
			}
		}
}
