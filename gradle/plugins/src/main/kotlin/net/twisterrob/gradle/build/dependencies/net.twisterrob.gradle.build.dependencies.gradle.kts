import net.twisterrob.gradle.build.dependencies.enableDependencyLocking
import net.twisterrob.gradle.build.dependencies.replaceGradlePluginAutoDependenciesWithoutKotlin
import net.twisterrob.gradle.build.dependencies.replaceHamcrestDependencies

enableDependencyLocking()

replaceGradlePluginAutoDependenciesWithoutKotlin()

configurations.all {
	replaceHamcrestDependencies(project)
}
