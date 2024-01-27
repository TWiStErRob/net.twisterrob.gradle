package net.twisterrob.gradle.build.root

import net.twisterrob.gradle.build.dsl.libs
import org.gradle.api.Project

internal fun assertKotlinVersion(project: Project) {
	@Suppress("detekt.MaxChainedCallsOnSameLine")
	val target: String = project.libs.versions.kotlin.target.get()
	@Suppress("detekt.MaxChainedCallsOnSameLine")
	val language: String = project.libs.versions.kotlin.language.get()
	check(target.startsWith(language)) {
		error("Kotlin target version ($target) must be compatible with language version ($language).")
	}
}
