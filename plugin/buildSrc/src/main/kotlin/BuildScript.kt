// it's used from project's build.gradle.kts files
@file:Suppress("unused")

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.FileCollectionDependency
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.internal.HasConvention
import org.gradle.api.internal.artifacts.dependencies.DefaultSelfResolvingDependency
import org.gradle.api.internal.artifacts.dsl.dependencies.DependencyFactory.ClassPathNotation
import org.gradle.api.internal.file.FileCollectionInternal
import org.gradle.api.plugins.BasePluginConvention
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.getPluginByName
import org.gradle.kotlin.dsl.gradleKotlinDsl
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension
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
fun SourceSet.kotlin(action: SourceDirectorySet.() -> Unit) {
	kotlin.action()
}

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

val Project.gradlePlugin: GradlePluginDevelopmentExtension
	get() = this.extensions.getByName<GradlePluginDevelopmentExtension>("gradlePlugin")

/**
 * Alternative solution: https://stackoverflow.com/a/64825340/253468
 */
fun Project.replaceGradlePluginAutoDependenciesWithoutKotlin() {
	plugins.withId("org.gradle.java-gradle-plugin") {
		@Suppress("DEPRECATION")
		dependencies {
			// Undo org.gradle.plugin.devel.plugins.JavaGradlePluginPlugin.applyDependencies
			if (configurations[JavaPlugin.COMPILE_CONFIGURATION_NAME].dependencies.remove(gradleApi())) {
				add(JavaPlugin.COMPILE_CONFIGURATION_NAME, gradleApiWithoutKotlin())
			}

			// Undo org.gradle.plugin.devel.plugins.JavaGradlePluginPlugin.TestKitAndPluginClasspathDependenciesAction
			afterEvaluate {
				gradlePlugin.testSourceSets.forEach {
					if (configurations[it.compileConfigurationName].dependencies.remove(gradleTestKit())) {
						add(it.compileConfigurationName, gradleTestKitWithoutKotlin())
					}
				}
			}
		}
	}

	plugins.withId("org.gradle.kotlin.kotlin-dsl.base") { // applied from org.gradle.kotlin.kotlin-dsl
		dependencies {
			// based on org.gradle.kotlin.dsl.plugins.embedded.kotlinArtifactConfigurationNames (in EmbeddedKotlinPlugin.kt)
			val kotlinArtifactConfigurationNames = listOf(
				JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME,
				JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME
			)
			kotlinArtifactConfigurationNames.forEach {
				// Undo org.gradle.kotlin.dsl.plugins.base.KotlinDslBasePlugin.addGradleKotlinDslDependencyTo
				if (configurations[it].dependencies.remove(gradleKotlinDsl())) {
					add(it, gradleKotlinDslWithoutKotlin())
				}
			}
		}
	}
}

fun DependencyHandler.gradleApiWithoutKotlin(): Dependency =
	withoutKotlin(ClassPathNotation.GRADLE_API)

fun DependencyHandler.gradleKotlinDslWithoutKotlin(): Dependency =
	withoutKotlin(ClassPathNotation.GRADLE_KOTLIN_DSL)

fun DependencyHandler.gradleTestKitWithoutKotlin(): Dependency =
	withoutKotlin(ClassPathNotation.GRADLE_TEST_KIT)

private fun DependencyHandler.withoutKotlin(notation: ClassPathNotation): Dependency {
	// Originally created in org.gradle.api.internal.notations.DependencyClassPathNotationConverter.create
	val gradleApi = create(notation) as FileCollectionDependency
	val filteredSource = gradleApi.files.filter { !it.name.startsWith("kotlin-") }
	val displayName = StaticComponentIdentifier("${notation.displayName} (without Kotlin)")
	return DefaultSelfResolvingDependency(displayName, filteredSource as FileCollectionInternal)
}

/**
 * Based on [org.gradle.internal.component.local.model.OpaqueComponentIdentifier] in Gradle 5.6.4.
 */
class StaticComponentIdentifier(private val displayName: String) : ComponentIdentifier {

	override fun getDisplayName(): String = displayName

	override fun equals(other: Any?): Boolean =
		when {
			this === other -> true
			other != null && this::class == other::class -> {
				val that = other as StaticComponentIdentifier
				this.displayName == that.displayName
			}
			else -> false
		}

	override fun hashCode(): Int = displayName.hashCode()

	override fun toString(): String = displayName
}
