package net.twisterrob.gradle.checkstyle

import net.twisterrob.gradle.common.BaseQualityExtension

@Suppress("detekt.AbstractClassCanBeConcreteClass") // Gradle convention.
abstract class CheckStyleExtension : BaseQualityExtension<CheckStyleTask>()
