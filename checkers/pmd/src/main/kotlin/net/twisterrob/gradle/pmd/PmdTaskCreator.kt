package net.twisterrob.gradle.pmd

import net.twisterrob.gradle.common.VariantTaskCreator
import org.gradle.api.Project
import java.io.File

class PmdTaskCreator(project: Project) : VariantTaskCreator<PmdTask>(
	project,
	"pmd",
	"pmd",
	PmdTask::class.java,
	PmdExtension::class.java
) {

	override fun taskConfigurator(): VariantTaskCreator<PmdTask>.DefaultTaskConfig =
		object : VariantTaskCreator<PmdTask>.DefaultTaskConfig() {

			override fun setupConfigLocations(task: PmdTask) {
				task.ruleSets = emptyList() // default is java-basic
				val rootConfig = task.project.rootProject.file("config/pmd/pmd.xml")
				val subConfig = task.project.file("config/pmd/pmd.xml")
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

			override fun setupSources(
				task: PmdTask,
				variants: Collection<@Suppress("DEPRECATION" /* AGP 7.0 */) com.android.build.gradle.api.BaseVariant>
			) {
				super.setupSources(task, variants)

				val buildPath = task.project.buildDir.toPath()
				val projectPath = task.project.projectDir.toPath()
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

				// Note: Kotlin 1.4 introduced Sequence.flatMap(()->Iterable), Gradle <6.8 uses Kotlin 1.3.x

				task.include(variants
					.asSequence()
					.flatMap { it.sourceSets.asSequence() }
					.flatMap { it.resDirectories.asSequence() }
					.map { dir ->
						// build relative path (e.g. src/main/res) and
						// append a trailing "/" for include to treat it as recursive
						projectPath.relativize(dir.toPath()).toString() + File.separator
					}
					.toList()
				)

				task.include(variants
					.asSequence()
					.flatMap { it.sourceSets.asSequence() }
					.map { it.manifestFile }
					.map { file ->
						// build relative path (e.g. src/main/AndroidManifest.xml)
						projectPath.relativize(file.toPath()).toString()
					}
					.toList()
				)
			}
		}
}
