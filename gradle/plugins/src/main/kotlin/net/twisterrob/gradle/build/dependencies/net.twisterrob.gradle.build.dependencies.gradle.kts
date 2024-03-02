import net.twisterrob.gradle.build.dependencies.enableDependencyLocking
import net.twisterrob.gradle.build.dependencies.replaceHamcrestDependencies

enableDependencyLocking()

configurations.all {
	replaceHamcrestDependencies(project)
}
