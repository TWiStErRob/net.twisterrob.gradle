// it's used from project's build.gradle.kts files
@file:Suppress("unused")

import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.internal.HasConvention
import org.gradle.api.plugins.BasePluginConvention
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getPluginByName
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

/**
 * @see <a href="https://github.com/JetBrains/kotlin/blob/v1.2.20/buildSrc/src/main/kotlin/sourceSets.kt#L45-L54">sourceSets.kt</a>
 * @see <a href="https://github.com/gradle/kotlin-dsl/issues/343#issuecomment-331636906">DSL</a>
 */
val SourceSet.kotlin: SourceDirectorySet
	get() =
		(this as HasConvention)
				.convention
				.getPlugin(KotlinSourceSet::class.java)
				.kotlin

/**
 * @see <a href="https://github.com/JetBrains/kotlin/blob/v1.2.20/buildSrc/src/main/kotlin/sourceSets.kt#L45-L54">sourceSets.kt</a>
 */
fun SourceSet.kotlin(action: SourceDirectorySet.() -> Unit) =
		kotlin.action()

/**
 * Pull in resources from other modules' `src/test/resources` folders.
 */
fun Project.pullTestResourcesFrom(projectPath: String) = pullTestResourcesFrom(evaluationDependsOn(projectPath))

private fun Project.pullTestResourcesFrom(project: Project) {
	val myResources = this.java.sourceSets["test"].resources
	val otherResources = project.java.sourceSets["test"].resources
	myResources.srcDirs(otherResources.srcDirs)
}

/**
 * @see <a href="file://.../gradle-kotlin-dsl-accessors/.../src/org/gradle/kotlin/dsl/accessors.kt">Generated code</a>
 */
val Project.java: JavaPluginConvention
	get() = convention.getPluginByName("java")

val Project.base: BasePluginConvention
	get() = convention.getPluginByName("base")
