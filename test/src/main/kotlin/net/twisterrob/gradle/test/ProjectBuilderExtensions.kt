@file:Suppress("NOTHING_TO_INLINE")

package net.twisterrob.gradle.test

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

inline fun ProjectBuilder(): ProjectBuilder =
	ProjectBuilder.builder()

inline fun Project(): Project =
	ProjectBuilder().build()

@Suppress("FunctionName")
inline fun RootProject(): Project =
	ProjectBuilder().build()

inline fun Project.buildSubProject(name: String): ProjectBuilder =
	ProjectBuilder().withName(name).withParent(this)

inline fun Project.createSubProject(name: String): Project =
	buildSubProject(name).build()
