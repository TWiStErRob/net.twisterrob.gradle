package net.twisterrob.gradle.checkstyle

import net.twisterrob.gradle.common.BaseQualityPlugin

class CheckStylePlugin extends BaseQualityPlugin {

	CheckStylePlugin() {
		super(CheckStyleTaskCreator)
	}
}
