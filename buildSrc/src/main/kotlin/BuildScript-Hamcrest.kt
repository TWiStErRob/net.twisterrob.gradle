import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.DependencyResolveDetails
import org.gradle.api.artifacts.VersionCatalog

fun Configuration.replaceHamcrestDependencies(project: Project) {
	// TODEL https://github.com/gradle/gradle/issues/19001
	@Suppress("UnstableApiUsage")
	val versionCatalog = project.rootProject.versionCatalogs.single()
	resolutionStrategy.eachDependency { replaceHamcrestDependencies(versionCatalog) }
}

/**
 * https://github.com/junit-team/junit4/pull/1608#issuecomment-496238766
 */
@Suppress("UnstableApiUsage")
private fun DependencyResolveDetails.replaceHamcrestDependencies(versionCatalog: VersionCatalog) {
	if (requested.group == "org.hamcrest") {
		when (requested.name) {
			"java-hamcrest" -> {
				useTarget(versionCatalog.findDependency("hamcrest").get())
				because("2.0.0.0 shouldn't have been published")
			}
			"hamcrest-core" -> { // Could be 1.3 (JUnit 4) or 2.x too.
				useTarget(versionCatalog.findDependency("hamcrest").get())
				because("hamcrest-core doesn't contain anything")
			}
			"hamcrest-library" -> {
				useTarget(versionCatalog.findDependency("hamcrest").get())
				because("hamcrest-library doesn't contain anything")
			}
		}
	}
}
