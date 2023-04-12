package net.twisterrob.gradle.build.deprecation

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.intellij.lang.annotations.Language

internal abstract class DeprecatedPluginGradleDescriptorGeneratingTask : DefaultTask() {

	@get:Input
	abstract val oldName: Property<String>

	@get:Input
	abstract val implementationClass: Property<String>

	@get:OutputDirectory
	abstract val output: DirectoryProperty

	@TaskAction
	fun generateDeprecatedPlugin() {
		@Language("properties")
		val deprecatedPluginDescriptor = """
			implementation-class=${implementationClass.get()}Deprecated
		""".trimIndent() + "\n"

		output.get().asFile.resolve("META-INF/gradle-plugins/${oldName.get()}.properties")
			.also { it.parentFile.mkdirs() }
			.writeText(deprecatedPluginDescriptor)
	}
}
