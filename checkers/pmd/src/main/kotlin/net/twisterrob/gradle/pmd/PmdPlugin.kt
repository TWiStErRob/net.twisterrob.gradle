package net.twisterrob.gradle.pmd

import net.twisterrob.gradle.common.BaseQualityPlugin

class PmdPlugin : BaseQualityPlugin(
	PmdTaskCreator::class.java,
	"pmd",
	PmdExtension::class.java
)
