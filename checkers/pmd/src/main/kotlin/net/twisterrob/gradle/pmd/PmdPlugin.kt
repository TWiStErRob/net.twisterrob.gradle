package net.twisterrob.gradle.pmd

import net.twisterrob.gradle.common.BaseQualityPlugin

abstract class PmdPlugin : BaseQualityPlugin(
	PmdTaskCreator::class.java,
	"pmd",
	PmdExtension::class.java
)
