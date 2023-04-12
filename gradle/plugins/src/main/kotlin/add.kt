// In default package so it "just works" in build.gradle.kts files.

import org.gradle.api.Action
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.DependencyHandlerScope

/**
 * Polyfill for not yet added method.
 * TODEL https://github.com/gradle/gradle/issues/18979
 */
fun DependencyHandlerScope.add(
	configurationName: String,
	dependency: Provider<MinimalExternalModuleDependency>,
	configuration: Action<in ExternalModuleDependency>
) {
	this@add.addProvider(configurationName, dependency, configuration)
}
