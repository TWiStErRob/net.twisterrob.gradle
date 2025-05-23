import net.twisterrob.gradle.nagging.doNotNagAboutForTest
import net.twisterrob.gradle.nagging.doNotNagAboutPatternForTest
import net.twisterrob.gradle.nagging.doNotNagAboutStackForTest
import java.io.File

initscript {
	dependencies {
		// Essentially:
		// classpath(files("test\\internal\\runtime\\build\\libs\\runtime-0.15-SNAPSHOT.jar"))
		val initscriptMetadata = File(System.getProperty("net.twisterrob.gradle.test.initscript-runtime"))
		java.util.Properties()
			.apply { load(initscriptMetadata.reader()) }
			.getProperty("initscript-classpath")
			.split(File.pathSeparator)
			.forEach { add("classpath", files(it)) }
	}
}

apply<net.twisterrob.gradle.nagging.NaggingPluginForTest>()

//System.setProperty("net.twisterrob.gradle.nagging.diagnostics", "true")

// Below nagging suppressions are sorted by (Gradle version, AGP version) lexicographically.

if ("@net.twisterrob.test.kotlin.pluginVersion@" < "1.9.0") {
	// https://youtrack.jetbrains.com/issue/KT-52976
	// > Configure project : in all Android tests
	// Example test: AndroidBuildPluginIntgTest.`can disable buildConfig decoration (debug)`
	doNotNagAboutStackForTest(
		"8.2" to "8.15",
		"0.0" to "100.0",
		"The org.gradle.api.plugins.Convention type has been deprecated. This is scheduled to be removed in Gradle 9.0. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#deprecated_access_to_conventions",
		"at org.jetbrains.kotlin.gradle.plugin.sources.android.configurator.GradleConventionAddKotlinSourcesToAndroidSourceSetConfigurator.configure(GradleConventionAddKotlinSourcesToAndroidSourceSetConfigurator.kt:30)",
	)
	// > Configure project : in all Kotlin tests
	// Example test: KotlinPluginIntgTest.`can compile Kotlin`
	doNotNagAboutStackForTest(
		"8.2" to "8.15",
		"0.0" to "100.0",
		"The org.gradle.api.plugins.Convention type has been deprecated. This is scheduled to be removed in Gradle 9.0. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#deprecated_access_to_conventions",
		"at org.jetbrains.kotlin.gradle.plugin.AbstractKotlinPlugin\$Companion\$setUpJavaSourceSets\$1.invoke(AbstractKotlinPlugin.kt:257)",
	)
}

if ("@net.twisterrob.test.kotlin.pluginVersion@" in "1.5.20".."2.0.10") {
	// https://youtrack.jetbrains.com/issue/KT-67838
	// > Configure project :
	// Example test: AndroidBuildPluginIntgTest.`can disable buildConfig decoration (debug)`
	// Introduced in 1.5.20: https://github.com/JetBrains/kotlin/commit/34e0a3caa890246946ec5fc0153a0b3dc4271244
	val lineNumber = when ("@net.twisterrob.test.kotlin.pluginVersion@") {
		in "1.8.0".."1.8.22" -> 406
		in "1.9.0".."1.9.10" -> 417
		in "1.9.20".."1.9.20" -> 418
		in "1.9.21".."1.9.25" -> 422
		in "2.0.0".."2.0.10" -> 405
		else -> error("Don't know the line number yet for @net.twisterrob.test.kotlin.pluginVersion@.")
	}
	doNotNagAboutStackForTest(
		"8.8" to "8.15",
		"0.0" to "100.0",
		"The Configuration.fileCollection(Spec) method has been deprecated. This is scheduled to be removed in Gradle 9.0. Use Configuration.getIncoming().artifactView(Action) with a componentFilter instead. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#deprecate_filtered_configuration_file_and_filecollection_methods",
		"at org.jetbrains.kotlin.gradle.internal.Kapt3GradleSubplugin\$createKaptKotlinTask\$2.invoke(Kapt3KotlinGradleSubplugin.kt:${lineNumber})",
	)
}

doNotNagAboutPatternForTest(
	"8.8" to "8.13",
	"8.2" to "8.3",
	// > Task :generateDebugRFile
	// > Task :generateReleaseRFile
	// Example test: AndroidBuildPluginIntgTest.`can disable buildConfig decoration (debug)`
	// Example test: KotlinPluginIntgTest.`can test kotlin with JUnit in Android Test App`
	"Mutating the dependencies of configuration '(:.+)*:(release|debug)CompileClasspath'" +
			Regex.escape(" after it has been resolved or consumed. This behavior has been deprecated. This will fail with an error in Gradle 9.0. After a Configuration has been resolved, consumed as a variant, or used for generating published metadata, it should not be modified. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#mutate_configuration_after_locking") + ".*",
	//"at com.android.build.gradle.internal.dependency.ConstraintHandler\$alignWith\$1\$1.execute(ConstraintHandler.kt:56)"
)
doNotNagAboutPatternForTest(
	"8.13" to "8.15",
	"8.2" to "8.3",
	// > Task :generateDebugRFile
	// > Task :generateReleaseRFile
	// Example test: AndroidBuildPluginIntgTest.`can disable buildConfig decoration (debug)`
	// Example test: KotlinPluginIntgTest.`can test kotlin with JUnit in Android Test App`
	Regex.escape("Mutating a configuration after it has been resolved, consumed as a variant, or used for generating published metadata. This behavior has been deprecated. This will fail with an error in Gradle 9.0. The dependencies of configuration '") + "(:.+)*:(release|debug)(UnitTest)?CompileClasspath" + Regex.escape(
		"' were mutated after the configuration was resolved. After a configuration has been observed, it should not be modified. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#mutate_configuration_after_locking"
	) + ".*",
	//"at com.android.build.gradle.internal.dependency.ConstraintHandler\$alignWith\$1\$1.execute(ConstraintHandler.kt:56)"
)

// https://github.com/gradle/gradle/issues/32422
// https://issuetracker.google.com/issues/370546370
doNotNagAboutForTest(
	"8.13" to "9.1",
	"8.2" to "8.10",
	// > Configure project :
	// Example test: AndroidBuildPluginIntgTest.`can override compileSdk (debug)`
	"Declaring an 'is-' property with a Boolean type has been deprecated. Starting with Gradle 9.0, this property will be ignored by Gradle. The combination of method name and return type is not consistent with Java Bean property rules and will become unsupported in future versions of Groovy. Add a method named 'getCrunchPngs' with the same behavior and mark the old one with @Deprecated, or change the type of 'com.android.build.gradle.internal.dsl.BuildType\$AgpDecorated.isCrunchPngs' (and the setter) to 'boolean'. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#groovy_boolean_properties",
)
doNotNagAboutForTest(
	"8.13" to "9.1",
	"8.2" to "8.10",
	// > Configure project :
	// Example test: AndroidBuildPluginIntgTest.`can override compileSdk (debug)`
	"Declaring an 'is-' property with a Boolean type has been deprecated. Starting with Gradle 9.0, this property will be ignored by Gradle. The combination of method name and return type is not consistent with Java Bean property rules and will become unsupported in future versions of Groovy. Add a method named 'getUseProguard' with the same behavior and mark the old one with @Deprecated, or change the type of 'com.android.build.gradle.internal.dsl.BuildType.isUseProguard' (and the setter) to 'boolean'. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#groovy_boolean_properties",
)
doNotNagAboutForTest(
	"8.13" to "9.1",
	"8.2" to "8.10",
	// > Configure project :
	// Example test: AndroidBuildPluginIntgTest.`can override compileSdk (debug)`
	"Declaring an 'is-' property with a Boolean type has been deprecated. Starting with Gradle 9.0, this property will be ignored by Gradle. The combination of method name and return type is not consistent with Java Bean property rules and will become unsupported in future versions of Groovy. Add a method named 'getWearAppUnbundled' with the same behavior and mark the old one with @Deprecated, or change the type of 'com.android.build.api.variant.impl.ApplicationVariantImpl.isWearAppUnbundled' (and the setter) to 'boolean'. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#groovy_boolean_properties",
)

// https://issuetracker.google.com/issues/408334529
doNotNagAboutStackForTest(
	"8.14" to "9.1",
	"8.4" to "8.10",
	// > Task :generateReleaseLintVitalReportModel
	// Example test: AndroidMinificationPluginIntgTest.`default build setup minifies only release using AndroidX (debug) and (release)`
	"Retrieving attribute with a null key. This behavior has been deprecated. This will fail with an error in Gradle 10.0. Don't request attributes from attribute containers using null keys. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#null-attribute-lookup",
	"at com.android.build.gradle.internal.ide.dependencies.ArtifactUtils.isAndroidProjectDependency(ArtifactUtils.kt:5",
)

if ("@net.twisterrob.test.kotlin.pluginVersion@" in "1.9.0".."1.9.25") {
	// https://youtrack.jetbrains.com/issue/KT-64355
	// https://youtrack.jetbrains.com/issue/KT-61457 was fixed, but then its fix deprecated again.
	// Fixed in 2.0.0: https://github.com/JetBrains/kotlin/commit/e4050724190c4d4a25ed3aea7a26e7535bfa3e9a
	// > Configure project : in all Kotlin tests
	// Example test: KotlinPluginIntgTest.`can compile Kotlin`
	doNotNagAboutStackForTest(
		"9.0" to "9.1",
		"0.0" to "100.0",
		"The StartParameter.isConfigurationCacheRequested property has been deprecated. This is scheduled to be removed in Gradle 10.0. Please use 'configurationCache.requested' property on 'BuildFeatures' service instead. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#deprecated_startparameter_is_configuration_cache_requested",
		"at org.jetbrains.kotlin.gradle.plugin.internal.DefaultConfigurationCacheStartParameterAccessor\$isConfigurationCacheRequested\$2.invoke(ConfigurationCacheStartParameterAccessor.kt:35)",
	)
}
