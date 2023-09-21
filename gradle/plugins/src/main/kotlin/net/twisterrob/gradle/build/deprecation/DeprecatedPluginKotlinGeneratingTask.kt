package net.twisterrob.gradle.build.deprecation

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.intellij.lang.annotations.Language

internal abstract class DeprecatedPluginKotlinGeneratingTask : DefaultTask() {

	@get:Input
	abstract val oldName: Property<String>

	@get:Input
	abstract val newName: Property<String>

	@get:Input
	abstract val implementationClass: Property<String>

	@get:OutputDirectory
	abstract val output: DirectoryProperty

	@TaskAction
	fun generateDeprecatedPlugin() {
		if (oldName.get() == newName.get()) {
			error("Old and new plugin ID are the same: ${oldName.get()}")
		}
		val implementationClass = implementationClass.get()
		val packageName = implementationClass.substringBeforeLast('.')
		val className = implementationClass.substringAfterLast('.')
		@Language("kotlin")
		@Suppress("ClassName")
		val deprecatedPluginSourceCode = """
			package ${packageName}
			
			import net.twisterrob.gradle.internal.deprecation.DeprecatedProjectPlugin
			
			@Suppress("UnnecessaryAbstractClass") // Gradle convention.
			internal abstract class ${className}Deprecated : DeprecatedProjectPlugin(
				oldName = "${oldName.get()}",
				newName = "${newName.get()}",
			)
		""".trimIndent() + "\n"

		val fileName = "${packageName.replace('.', '/')}/${className}Deprecated.kt"
		output.get().asFile.resolve(fileName)
			.also { it.parentFile.mkdirs() }
			.writeText(deprecatedPluginSourceCode)
	}
}
