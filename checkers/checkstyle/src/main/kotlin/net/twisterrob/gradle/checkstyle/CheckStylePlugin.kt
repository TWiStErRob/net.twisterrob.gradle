package net.twisterrob.gradle.checkstyle

import net.twisterrob.gradle.common.BaseQualityPlugin

@Suppress("UnnecessaryAbstractClass") // Gradle convention.
abstract class CheckStylePlugin : BaseQualityPlugin(
	CheckStyleTaskCreator::class.java,
	"checkstyle",
	CheckStyleExtension::class.java
)
