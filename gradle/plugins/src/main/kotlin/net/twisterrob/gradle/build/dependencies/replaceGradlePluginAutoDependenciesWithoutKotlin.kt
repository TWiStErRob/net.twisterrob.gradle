package net.twisterrob.gradle.build.dependencies

import net.twisterrob.gradle.build.dsl.gradlePlugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.gradleKotlinDsl

/**
 * Alternative solution: https://stackoverflow.com/a/64825340/253468
 */
fun Project.replaceGradlePluginAutoDependenciesWithoutKotlin() {
	plugins.withId("org.gradle.java-gradle-plugin") {
		dependencies {
			// Undo org.gradle.plugin.devel.plugins.JavaGradlePluginPlugin.applyDependencies
			if (configurations[JavaPlugin.API_CONFIGURATION_NAME].dependencies.remove(gradleApi())) {
				add(JavaPlugin.API_CONFIGURATION_NAME, gradleApiWithoutKotlin())
			}

			// Undo org.gradle.plugin.devel.plugins.JavaGradlePluginPlugin.TestKitAndPluginClasspathDependenciesAction
			afterEvaluate {
				gradlePlugin.testSourceSets.forEach {
					if (configurations[it.implementationConfigurationName].dependencies.remove(gradleTestKit())) {
						add(it.implementationConfigurationName, gradleTestKitWithoutKotlin())
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
