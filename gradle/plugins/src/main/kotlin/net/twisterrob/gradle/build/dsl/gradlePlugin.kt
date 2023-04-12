package net.twisterrob.gradle.build.dsl

import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByName
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension

val Project.gradlePlugin: GradlePluginDevelopmentExtension
	get() = this.extensions.getByName<GradlePluginDevelopmentExtension>("gradlePlugin")

