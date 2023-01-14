import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.tasks.Copy
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project
import org.gradle.kotlin.dsl.register

fun Project.exposeTestResources() {
	val packageTestResources = tasks.register<Copy>("packageTestResources") {
		from(project.java.sourceSets.named("test").map { it.resources })
		into(project.layout.buildDirectory.dir("packagedTestResources"))
	}
	val testResources = configurations.create("exposedTestResources")
	artifacts {
		add(testResources.name, packageTestResources)
	}
}

/**
 * Pull in resources from other modules' `src/test/resources` folders.
 */
fun Project.pullTestResourcesFrom(project: ProjectDependency) {
	val testResources = configurations.create("gobbledTestResources")
	dependencies {
		add(testResources.name, project(project.dependencyProject.path, configuration = "exposedTestResources"))
	}
	val copyResources = tasks.register<Copy>("copyExposedTestResources") {
		from(testResources)
		into(layout.buildDirectory.dir("unPackagedTestResources"))
	}
	java.sourceSets.named("test").configure {
		resources.srcDir(copyResources)
	}
}
