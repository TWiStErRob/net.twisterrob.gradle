package net.twisterrob.gradle.dsl

import org.gradle.api.Project
import org.gradle.api.UnknownTaskException
import org.gradle.api.reporting.ReportingExtension
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.getByName

val Project.reporting: ReportingExtension
	get() = this.extensions.getByName<ReportingExtension>(ReportingExtension.NAME)

operator fun TaskContainer.contains(name: String): Boolean =
	try {
		this.named(name)
		true
	} catch (ex: UnknownTaskException) {
		false
	}
