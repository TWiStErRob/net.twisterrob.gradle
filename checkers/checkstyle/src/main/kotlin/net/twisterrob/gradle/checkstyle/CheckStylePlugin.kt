package net.twisterrob.gradle.checkstyle

import net.twisterrob.gradle.common.BaseQualityPlugin

@Suppress("detekt.AbstractClassCanBeConcreteClass") // Gradle convention.
abstract class CheckStylePlugin : BaseQualityPlugin(
	CheckStyleTaskCreator::class.java,
	"checkstyle",
	CheckStyleExtension::class.java
)
