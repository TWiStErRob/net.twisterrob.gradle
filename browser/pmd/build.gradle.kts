plugins {
	id("net.twisterrob.gradle.build.module.browser")
	id("org.gradle.pmd")
}

configurations {
	// Add checkstyle to a known configuration, so that it's resolved by IDEA Gradle Sync import.
	compileOnly.configure { extendsFrom(pmd.get()) }
}

pmd {
	toolVersion = PmdPlugin.DEFAULT_PMD_VERSION
}
