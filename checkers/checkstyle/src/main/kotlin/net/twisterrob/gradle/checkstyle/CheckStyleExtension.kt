package net.twisterrob.gradle.checkstyle

import net.twisterrob.gradle.common.BaseQualityExtension
import org.gradle.api.Project

open class CheckStyleExtension(project: Project) : BaseQualityExtension<CheckStyleTask>(project)
