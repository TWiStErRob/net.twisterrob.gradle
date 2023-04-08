plugins {
	id("net.twisterrob.gradle.build.module.browser")
	id("org.gradle.pmd")
}

pmd {
	toolVersion = PmdPlugin.DEFAULT_PMD_VERSION
}

configurations {
	sourcesOnly.configure { extendsFrom(pmd.get()) }
}
