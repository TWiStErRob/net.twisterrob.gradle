plugins {
	id("net.twisterrob.gradle.build.module.browser")
	id("org.gradle.checkstyle")
}

configurations {
	// Add checkstyle to a known configuration, so that it's resolved by IDEA Gradle Sync import.
	compileOnly.configure { extendsFrom(checkstyle.get()) }
}

checkstyle {
	toolVersion = CheckstylePlugin.DEFAULT_CHECKSTYLE_VERSION
}
