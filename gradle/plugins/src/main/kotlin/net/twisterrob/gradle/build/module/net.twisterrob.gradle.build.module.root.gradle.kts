import net.twisterrob.gradle.build.detekt.DetektRootPlugin
import net.twisterrob.gradle.build.root.assertKotlinVersion

plugins {
	id("net.twisterrob.gradle.build.root.idea")
	id("net.twisterrob.gradle.build.root.publishing")
	id("net.twisterrob.gradle.build.root.allDependencies")
	id("net.twisterrob.gradle.build.root.externals")
}

plugins.apply(DetektRootPlugin::class)

assertKotlinVersion(project)
