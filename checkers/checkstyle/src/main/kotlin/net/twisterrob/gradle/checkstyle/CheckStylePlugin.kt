package net.twisterrob.gradle.checkstyle

import net.twisterrob.gradle.common.BaseQualityPlugin

abstract class CheckStylePlugin : BaseQualityPlugin(
	CheckStyleTaskCreator::class.java,
	"checkstyle",
	CheckStyleExtension::class.java
)
