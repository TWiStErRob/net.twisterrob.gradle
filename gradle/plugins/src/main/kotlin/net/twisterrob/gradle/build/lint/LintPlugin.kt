package net.twisterrob.gradle.build.lint

import com.android.build.api.dsl.Lint
import net.twisterrob.gradle.build.dsl.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class LintPlugin : Plugin<Project> {
	
	override fun apply(project: Project) {
		project.pluginManager.apply("com.android.lint")
		project.dependencies.add("lintChecks", project.libs.androidx.lint)
		project.configure<Lint> {
			warningsAsErrors = true
			checkAllWarnings = true
			checkTestSources = true
			val rootDir = project.isolated.rootProject.projectDirectory
			lintConfig = rootDir.file("config/lint/lint.xml").asFile
			val slug = project.path.replace(":", "-")
			baseline = rootDir.file("config/lint/lint-baseline${slug}.xml").asFile
		}
	}
}
