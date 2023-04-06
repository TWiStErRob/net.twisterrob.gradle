import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.tasks.Sync
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project
import org.gradle.kotlin.dsl.register

/**
 * Expose test resources so another project can use the same files without hacking.
 */
fun Project.exposeTestResources() {
	val packageTestResources = tasks.register<Sync>("packageTestResources") {
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
	// This copy is necessary to deduplicate source roots and prevent IDEA warning:
	// > Duplicate content roots detected:
	// > Path [test/src/test/resources] of module [checkstyle.test] was removed from modules [pmd.test, quality.test]
	val copyResources = tasks.register<Sync>("copyExposedTestResources") {
		from(testResources)
		into(layout.buildDirectory.dir("unPackagedTestResources"))
	}
	java.sourceSets.named("test").configure {
		resources.srcDir(copyResources)
	}
}
