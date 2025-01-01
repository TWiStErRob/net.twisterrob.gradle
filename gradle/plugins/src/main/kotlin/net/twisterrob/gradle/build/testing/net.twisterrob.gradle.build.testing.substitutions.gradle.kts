tasks.withType<ProcessResources>().configureEach {
	val propertyNamesToReplace = listOf(
		"net.twisterrob.test.android.pluginVersion",
		"net.twisterrob.test.kotlin.pluginVersion",
		"net.twisterrob.test.android.compileSdkVersion"
	)
	val properties = propertyNamesToReplace.associateWith(providers::gradleProperty)
	// TODEL https://github.com/gradle/gradle/issues/861
	properties.forEach { (name, value) -> inputs.property(name, value) }
	val processedFiles = listOf(
		"**/build.gradle",
		"**/build.gradle.kts",
		"**/settings.gradle",
		"**/settings.gradle.kts",
		"**/gradle.properties",
		"**/*.init.gradle.kts",
	)
	// TODEL https://github.com/gradle/gradle/issues/24698
	inputs.property("processedFiles", processedFiles)
	filesMatching(processedFiles) {
		val replacements = properties.mapValues { it.value.get() } + mapOf(
			// custom replacements (`"name" to value`) would come here
		)
		filter(mapOf("tokens" to replacements), org.apache.tools.ant.filters.ReplaceTokens::class.java)
	}
}
