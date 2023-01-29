import net.twisterrob.gradle.slug
import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.dsl.DependencyLockingHandler
import org.gradle.api.initialization.Settings
import org.gradle.api.initialization.dsl.ScriptHandler
import org.gradle.kotlin.dsl.dependencyLocking
import java.io.File

/**
 * Use in build.gradle.kts:
 * ```
 * enableDependencyLocking()
 * ```
 * or in root build.gradle.kts:
 * ```
 * allprojects { enableDependencyLocking() }
 * ```
 */
fun Project.enableDependencyLocking() {
	configurations.activateDependencyLocking()
	dependencyLocking {
		relocateLockFile(rootDir, slug)
	}
}

/**
 * Use in build.gradle.kts:
 * ```
 * buildscript { enableDependencyLocking(project) }
 * ```
 */
fun ScriptHandler.enableDependencyLocking(project: Project) {
	configurations.activateDependencyLocking()
	dependencyLocking {
		relocateLockFile(project.rootDir, project.slug + "-buildscript")
	}
}

/**
 * Use in settings.gradle.kts:
 * ```
 * buildscript { enableDependencyLocking(settings) }
 * ```
 */
fun ScriptHandler.enableDependencyLocking(settings: Settings) {
	configurations.activateDependencyLocking()
	dependencyLocking {
		relocateLockFile(settings.settingsDir, "settings-buildscript")
	}
}

private fun DependencyLockingHandler.relocateLockFile(root: File, name: String) {
	lockFile.set(root.resolve("gradle/dependency-locks/$name.lockfile"))
}

private fun ConfigurationContainer.activateDependencyLocking() {
	configureEach {
		when {
			// Reduce noise in the lock file, because DependenciesMetadata is redundant when looking at the final classpath.
			name.endsWith("DependenciesMetadata") -> resolutionStrategy.deactivateDependencyLocking()
			// Reduce noise in the lock file, because Dokka creates a ton of configurations.
			name.startsWith("dokka") -> resolutionStrategy.deactivateDependencyLocking()
			// For everything else, enable it. This will help discover changes and learn about new configurations.
			else -> resolutionStrategy.activateDependencyLocking()
		}
	}
}
