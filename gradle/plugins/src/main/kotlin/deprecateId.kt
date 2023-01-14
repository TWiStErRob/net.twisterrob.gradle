import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.register
import org.gradle.plugin.devel.PluginDeclaration
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension

/**
 * @see org.gradle.api.tasks.SourceSetOutput for original idea
 */
fun PluginDeclaration.deprecateId(project: Project, oldId: String) {
	val plugin = this
	val taskName = "generate${oldId.capitalize()}To${id.capitalize()}DeprecationPlugin"
	project.kotlinExtension.sourceSets.named("main").configure {
		kotlin.srcDir(
			project.tasks.register<DeprecatedPluginKotlinGeneratingTask>(taskName + "Sources") {
				oldName.set(oldId)
				newName.set(plugin.id)
				implementationClass.set(plugin.implementationClass)
				output.set(project.layout.buildDirectory.dir("plugin-deprecations/main/kotlin"))
			}
		)
	}
	project.java.sourceSets.named("main").configure {
		resources.srcDir(
			project.tasks.register<DeprecatedPluginGradleDescriptorGeneratingTask>(taskName + "Resources") {
				oldName.set(oldId)
				implementationClass.set(plugin.implementationClass)
				output.set(project.layout.buildDirectory.dir("plugin-deprecations/main/resources"))
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
		val implementationClass = implementationClass.get()

		@Language("kotlin")
		val deprecatedPluginSourceCode = """
			package ${implementationClass.substringBeforeLast('.')}
			
			import net.twisterrob.gradle.common.DeprecatedProjectPlugin
			
			internal class ${implementationClass.substringAfterLast('.')}Deprecated : DeprecatedProjectPlugin(
				oldName = "${oldName.get()}",
				newName = "${newName.get()}",
			)
		""".trimIndent()

		val fileName = "${implementationClass.substringAfterLast('.')}Deprecated.kt"
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
		""".trimIndent()

		output.get().asFile.resolve("META-INF/gradle-plugins/${oldName.get()}.properties")
			.also { it.parentFile.mkdirs() }
			.writeText(deprecatedPluginDescriptor)
	}
}
