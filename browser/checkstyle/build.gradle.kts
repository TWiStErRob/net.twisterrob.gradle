plugins {
	id("net.twisterrob.gradle.build.module.browser")
	id("org.gradle.checkstyle")
}

checkstyle {
	toolVersion = CheckstylePlugin.DEFAULT_CHECKSTYLE_VERSION
}

configurations {
	sourcesOnly.configure { extendsFrom(checkstyle.get()) }
}
