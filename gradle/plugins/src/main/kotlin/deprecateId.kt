@file:Suppress("MissingPackageDeclaration") // In default package so it "just works" in build.gradle.kts files.

import net.twisterrob.gradle.build.deprecation.DeprecatedPluginGradleDescriptorGeneratingTask
import net.twisterrob.gradle.build.deprecation.DeprecatedPluginKotlinGeneratingTask
import net.twisterrob.gradle.build.dsl.java
import org.gradle.api.Project
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.register
import org.gradle.plugin.devel.PluginDeclaration
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension

/**
 * @see org.gradle.api.tasks.SourceSetOutput for original idea
 */
fun PluginDeclaration.deprecateId(project: Project, oldId: String) {
	val plugin = this
	val taskName = "generate${oldId.replaceFirstChar(Char::uppercase)}To${id.replaceFirstChar(Char::uppercase)}DeprecationPlugin"
	project.kotlinExtension.sourceSets.named("main").configure {
		kotlin.srcDir(
			project.tasks.register<DeprecatedPluginKotlinGeneratingTask>(taskName + "Sources") {
				oldName = oldId
				newName = plugin.id
				implementationClass = plugin.implementationClass
				output = project.layout.buildDirectory.dir("plugin-deprecations/${oldId}/kotlin")
			}
		)
	}
	project.java.sourceSets.named("main").configure {
		resources.srcDir(
			project.tasks.register<DeprecatedPluginGradleDescriptorGeneratingTask>(taskName + "Resources") {
				oldName = oldId
				implementationClass = plugin.implementationClass
				output = project.layout.buildDirectory.dir("plugin-deprecations/${oldId}/resources")
			}
		)
	}
}
