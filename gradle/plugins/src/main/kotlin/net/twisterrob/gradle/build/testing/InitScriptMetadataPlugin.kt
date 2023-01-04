package net.twisterrob.gradle.build.testing

import StaticComponentIdentifier
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.file.RegularFile
import org.gradle.api.internal.lambdas.SerializableLambdas
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.testing.Test
import org.gradle.internal.component.local.model.OpaqueComponentIdentifier
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register

/**
 * @see org.gradle.plugin.devel.plugins.JavaGradlePluginPlugin
 */
class InitScriptMetadataPlugin : Plugin<Project> {

	override fun apply(project: Project) {
		val initScriptConfiguration = project.createClasspath()
		val initScriptTestMetadataTask = project.createMetadataTask(initScriptConfiguration)
		val initScriptClasspathFile = initScriptTestMetadataTask.flatMap { it.output }
		project.wireTestInitScriptClasspath(initScriptClasspathFile)
	}

	private fun Project.createClasspath(): Provider<Configuration> {
		@Suppress("UnstableApiUsage")
		val testRuntime = this.dependencyFactory.create(project(":test:internal:runtime"))
		return this.configurations.register(DEFAULT_CONFIGURATION_NAME) {
			isCanBeConsumed = false
			isCanBeResolved = true
			dependencies.add(testRuntime)
		}
	}

	/**
	 * @see net.twisterrob.gradle.build.testing.InitScriptTestMetadata
	 * @see org.gradle.plugin.devel.plugins.JavaGradlePluginPlugin.createAndConfigurePluginUnderTestMetadataTask
	 */
	private fun Project.createMetadataTask(classpath: Provider<Configuration>): TaskProvider<InitScriptTestMetadata> =
		this.tasks.register<InitScriptTestMetadata>("initScriptTestMetadata") {
			initScriptClasspath.from(classpath.map { configuration ->
				configuration
					.incoming
					.artifactView {
						componentFilter(SerializableLambdas.spec { excludeGradleApi(it) })
					}
					.files
			})
		}

	private fun Project.wireTestInitScriptClasspath(metadataFile: Provider<RegularFile>) {
		this.tasks.named<Test>("test").configure test@{
			// Add reference to the file generated by the task so that it's always generated.
			this@test.inputs.file(metadataFile)
				.withPathSensitivity(PathSensitivity.RELATIVE)
				.normalizeLineEndings()

			this@test.doFirst {
				val path = metadataFile.get().asFile.absolutePath
				this@test.systemProperty("net.twisterrob.gradle.test.initscript-runtime", path)
			}
		}
	}

	companion object {
		private const val DEFAULT_CONFIGURATION_NAME: String = "initscriptRuntimeClasspath"
	}
}

private fun excludeGradleApi(componentId: ComponentIdentifier): Boolean =
	when (componentId) {
		// Gradle built-in internal DependencyFactoryInternal.ClassPathNotation
		is OpaqueComponentIdentifier -> false
		// My override of DependencyFactoryInternal.ClassPathNotation, needs to be ignored the same asb above.
		is StaticComponentIdentifier -> false
		// Ignore Kotlin stdlib and related libraries, Gradle will provide the right version for those.
		is ModuleComponentIdentifier -> !componentId.group.startsWith("org.jetbrains")
		// Internal project(":...") dependencies are fine, that's why this whole hack exists.
		is ProjectComponentIdentifier -> true
		// Anything else is a problem because it's unclear what to do with them.
		else -> error("Unknown component identifier (${componentId::class.java}): ${componentId}")
	}