package net.twisterrob.gradle.build.dsl

import org.gradle.api.Project
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.kotlin.dsl.getByName

val Project.base: BasePluginExtension
	get() = this.extensions.getByName<BasePluginExtension>("base")

