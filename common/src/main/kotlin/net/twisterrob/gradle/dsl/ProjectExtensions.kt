package net.twisterrob.gradle.dsl

import org.gradle.api.Project
import org.gradle.api.reporting.ReportingExtension
import org.gradle.kotlin.dsl.getByName

val Project.reporting: ReportingExtension
	get() = this.extensions.getByName<ReportingExtension>(ReportingExtension.NAME)
