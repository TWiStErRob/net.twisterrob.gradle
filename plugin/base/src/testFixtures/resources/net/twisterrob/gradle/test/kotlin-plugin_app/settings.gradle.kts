import org.gradle.util.GradleVersion

buildscript {
	repositories {
		gradlePluginPortal()
	}
	dependencies {
		classpath("org.gradle.toolchains:foojay-resolver:0.5.0")
	}
}

if (GradleVersion.version("7.6") <= GradleVersion.current().baseVersion) {
	apply(plugin = "org.gradle.toolchains.foojay-resolver-convention")
}
