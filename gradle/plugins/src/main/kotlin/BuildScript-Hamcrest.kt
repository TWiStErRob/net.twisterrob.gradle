import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.DependencyResolveDetails
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.provider.Provider

fun Configuration.replaceHamcrestDependencies(project: Project) {
	// TODEL https://github.com/gradle/gradle/issues/15383#ref-issue-1054658077
	// Note: project.libs.hamcrest errors with NoClassDefFoundError: LibrariesForLibs
	val versionCatalog = project.rootProject.versionCatalogs.named("libs")
	val hamcrest = versionCatalog.findLibrary("hamcrest").get()
	resolutionStrategy.eachDependency { replaceHamcrestDependencies(hamcrest) }
}

/**
 * https://github.com/junit-team/junit4/pull/1608#issuecomment-496238766
 */
private fun DependencyResolveDetails.replaceHamcrestDependencies(hamcrest: Provider<MinimalExternalModuleDependency>) {
	if (requested.group == "org.hamcrest") {
		when (requested.name) {
			"java-hamcrest" -> {
				useTarget(hamcrest)
				because("2.0.0.0 shouldn't have been published")
			}
			"hamcrest-core" -> { // Could be 1.3 (JUnit 4) or 2.x too.
				useTarget(hamcrest)
				because("hamcrest-core doesn't contain anything")
			}
			"hamcrest-library" -> {
				useTarget(hamcrest)
				because("hamcrest-library doesn't contain anything")
			}
		}
	}
}
