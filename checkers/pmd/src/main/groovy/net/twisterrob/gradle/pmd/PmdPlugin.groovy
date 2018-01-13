package net.twisterrob.gradle.pmd

import net.twisterrob.gradle.common.BaseQualityPlugin

class PmdPlugin extends BaseQualityPlugin {

	PmdPlugin() {
		super(PmdTaskCreator)
	}
}
