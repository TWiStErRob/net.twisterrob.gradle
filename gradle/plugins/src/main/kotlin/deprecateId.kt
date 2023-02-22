import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.register
import org.gradle.plugin.devel.PluginDeclaration
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension

/**
 * @see org.gradle.api.tasks.SourceSetOutput for original idea
 */
fun PluginDeclaration.deprecateId(project: Project, oldId: String) {
	val plugin = this
	val taskName = "generate${oldId.capitalized()}To${id.capitalized()}DeprecationPlugin"
	project.kotlinExtension.sourceSets.named("main").configure {
		kotlin.srcDir(
			project.tasks.register<DeprecatedPluginKotlinGeneratingTask>(taskName + "Sources") {
				oldName.set(oldId)
				newName.set(plugin.id)
				implementationClass.set(plugin.implementationClass)
				output.set(project.layout.buildDirectory.dir("plugin-deprecations/${oldId}/kotlin"))
			}
		)
	}
	project.java.sourceSets.named("main").configure {
		resources.srcDir(
			project.tasks.register<DeprecatedPluginGradleDescriptorGeneratingTask>(taskName + "Resources") {
				oldName.set(oldId)
				implementationClass.set(plugin.implementationClass)
				output.set(project.layout.buildDirectory.dir("plugin-deprecations/${oldId}/resources"))
			}
		)
	}
}

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
		val deprecatedPluginSourceCode = """
			package ${packageName}
			
			import net.twisterrob.gradle.internal.deprecation.DeprecatedProjectPlugin
			
			internal class ${className}Deprecated : DeprecatedProjectPlugin(
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
