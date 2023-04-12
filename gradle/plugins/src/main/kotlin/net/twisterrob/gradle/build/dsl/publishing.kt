package net.twisterrob.gradle.build.dsl

import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.kotlin.dsl.getByName

val Project.publishing: PublishingExtension
	get() = this.extensions.getByName<PublishingExtension>("publishing")
