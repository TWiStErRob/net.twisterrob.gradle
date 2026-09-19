package net.twisterrob.gradle.pmd

import net.twisterrob.gradle.common.BaseQualityExtension

@Suppress("detekt.AbstractClassCanBeConcreteClass") // Gradle convention.
abstract class PmdExtension : BaseQualityExtension<PmdTask>()
