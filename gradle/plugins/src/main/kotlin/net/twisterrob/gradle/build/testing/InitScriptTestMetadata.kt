package net.twisterrob.gradle.build.testing;

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.util.PropertiesUtils
import org.gradle.work.DisableCachingByDefault
import java.io.File
import java.util.Properties

/**
 * @see net.twisterrob.gradle.build.testing.InitScriptMetadataPlugin.createMetadataTask
 * @see org.gradle.plugin.devel.tasks.PluginUnderTestMetadata
 */
@DisableCachingByDefault(because = "Not worth caching.")
abstract class InitScriptTestMetadata : DefaultTask() {

	@get:Classpath
	abstract val initScriptClasspath: ConfigurableFileCollection

	/**
	 * Covered by [initScriptClasspath], but has to be declared explicitly as input,
	 * so that it doesn't cross-pollinate executions in different checkout locations.
	 */
	@get:Input
	val paths: List<String>
		get() = initScriptClasspath.files.map { file: File ->
			file.absolutePath.replace(Regex("""\\"""), "/")
		}

	@get:OutputFile
	abstract val output: RegularFileProperty

	init {
		group = "Plugin development" // JavaGradlePluginPlugin.PLUGIN_DEVELOPMENT_GROUP
		description = "Generates the metadata for init script used in plugin integration tests."
		@Suppress("LeakingThis")
		output.convention(project.layout.buildDirectory.file("initscript-test-metadata.properties"))
	}

	@TaskAction
	fun generate() {
		val properties = Properties().apply {
			setProperty("initscript-classpath", paths.joinToString(File.pathSeparator))
		}
		PropertiesUtils.store(properties, output.get().asFile)
	}
}
