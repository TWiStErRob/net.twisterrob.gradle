package net.twisterrob.gradle.pmd

import net.twisterrob.gradle.common.BaseQualityPlugin

@Suppress("detekt.AbstractClassCanBeConcreteClass") // Gradle convention.
abstract class PmdPlugin : BaseQualityPlugin(
	PmdTaskCreator::class.java,
	"pmd",
	PmdExtension::class.java
)
