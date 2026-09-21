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
	"8.13" to "9.0",
	"8.2" to "8.11",
	// > Configure project :
	// Example test: AndroidBuildPluginIntgTest.`can override compileSdk (debug)`
	"Declaring an 'is-' property with a Boolean type has been deprecated. Starting with Gradle 9.0, this property will be ignored by Gradle. The combination of method name and return type is not consistent with Java Bean property rules and will become unsupported in future versions of Groovy. Add a method named 'getCrunchPngs' with the same behavior and mark the old one with @Deprecated, or change the type of 'com.android.build.gradle.internal.dsl.BuildType\$AgpDecorated.isCrunchPngs' (and the setter) to 'boolean'. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#groovy_boolean_properties",
)
doNotNagAboutForTest(
	"8.13" to "9.0",
	"8.2" to "8.11",
	// > Configure project :
	// Example test: AndroidBuildPluginIntgTest.`can override compileSdk (debug)`
	"Declaring an 'is-' property with a Boolean type has been deprecated. Starting with Gradle 9.0, this property will be ignored by Gradle. The combination of method name and return type is not consistent with Java Bean property rules and will become unsupported in future versions of Groovy. Add a method named 'getUseProguard' with the same behavior and mark the old one with @Deprecated, or change the type of 'com.android.build.gradle.internal.dsl.BuildType.isUseProguard' (and the setter) to 'boolean'. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#groovy_boolean_properties",
)
doNotNagAboutForTest(
	"8.13" to "9.0",
	"8.2" to "8.11",
	// > Configure project :
	// Example test: AndroidBuildPluginIntgTest.`can override compileSdk (debug)`
	"Declaring an 'is-' property with a Boolean type has been deprecated. Starting with Gradle 9.0, this property will be ignored by Gradle. The combination of method name and return type is not consistent with Java Bean property rules and will become unsupported in future versions of Groovy. Add a method named 'getWearAppUnbundled' with the same behavior and mark the old one with @Deprecated, or change the type of 'com.android.build.api.variant.impl.ApplicationVariantImpl.isWearAppUnbundled' (and the setter) to 'boolean'. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#groovy_boolean_properties",
)

// https://github.com/gradle/gradle/issues/32422
// https://issuetracker.google.com/issues/370546370
doNotNagAboutForTest(
	"9.0" to "9.1",
	"8.2" to "8.11",
	// > Configure project :
	// Example test: AndroidBuildPluginIntgTest.`can override compileSdk (debug)`
	"Declaring 'crunchPngs' as a property using an 'is-' method with a Boolean type on com.android.build.gradle.internal.dsl.BuildType\$AgpDecorated has been deprecated. Starting with Gradle 10, this property will no longer be treated like a property. The combination of method name and return type is not consistent with Java Bean property rules. Add a method named 'getCrunchPngs' with the same behavior and mark the old one with @Deprecated, or change the type of 'com.android.build.gradle.internal.dsl.BuildType\$AgpDecorated.isCrunchPngs' (and the setter) to 'boolean'. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#groovy_boolean_properties",
)
doNotNagAboutForTest(
	"9.0" to "9.1",
	"8.2" to "8.11",
	// > Configure project :
	// Example test: AndroidBuildPluginIntgTest.`can override compileSdk (debug)`
	"Declaring 'useProguard' as a property using an 'is-' method with a Boolean type on com.android.build.gradle.internal.dsl.BuildType has been deprecated. Starting with Gradle 10, this property will no longer be treated like a property. The combination of method name and return type is not consistent with Java Bean property rules. Add a method named 'getUseProguard' with the same behavior and mark the old one with @Deprecated, or change the type of 'com.android.build.gradle.internal.dsl.BuildType.isUseProguard' (and the setter) to 'boolean'. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#groovy_boolean_properties",
)
doNotNagAboutForTest(
	"9.0" to "9.1",
	"8.2" to "8.11",
	// > Configure project :
	// Example test: AndroidBuildPluginIntgTest.`can override compileSdk (debug)`
	"Declaring 'wearAppUnbundled' as a property using an 'is-' method with a Boolean type on com.android.build.api.variant.impl.ApplicationVariantImpl has been deprecated. Starting with Gradle 10, this property will no longer be treated like a property. The combination of method name and return type is not consistent with Java Bean property rules. Add a method named 'getWearAppUnbundled' with the same behavior and mark the old one with @Deprecated, or change the type of 'com.android.build.api.variant.impl.ApplicationVariantImpl.isWearAppUnbundled' (and the setter) to 'boolean'. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#groovy_boolean_properties",
)

// https://issuetracker.google.com/issues/408334529
doNotNagAboutStackForTest(
	"8.14" to "9.0",
	"8.4" to "8.12",
	// > Task :generateReleaseLintVitalReportModel
	// Example test: AndroidMinificationPluginIntgTest.`default build setup minifies only release using AndroidX (debug) and (release)`
	"Retrieving attribute with a null key. This behavior has been deprecated. This will fail with an error in Gradle 10.0. Don't request attributes from attribute containers using null keys. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#null-attribute-lookup",
	"at com.android.build.gradle.internal.ide.dependencies.ArtifactUtils.isAndroidProjectDependency(ArtifactUtils.kt:5",
)

// https://issuetracker.google.com/issues/408334529
doNotNagAboutStackForTest(
	"9.0" to "9.1",
	"8.4" to "8.12",
	// > Task :generateReleaseLintVitalReportModel
	// Example test: AndroidMinificationPluginIntgTest.`default build setup minifies only release using AndroidX (debug) and (release)`
	"Retrieving attribute with a null key. This behavior has been deprecated. This will fail with an error in Gradle 10. Don't request attributes from attribute containers using null keys. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#null-attribute-lookup",
	"at com.android.build.gradle.internal.ide.dependencies.ArtifactUtils.isAndroidProjectDependency(ArtifactUtils.kt:5",
)

// Gradle 9.1 vs AGP 8.13 https://issuetracker.google.com/issues/444260628
@Suppress("detekt.MaxLineLength")
doNotNagAboutPatternForTest(
	"9.1" to "9.2",
	"8.2" to "8.14", // Lower bound unconfirmed.
	Regex.escape("Declaring dependencies using multi-string notation has been deprecated. This will fail with an error in Gradle 10. Please use single-string notation instead: \"com.android.tools.build:aapt2:") + "\\d+\\.\\d+\\.\\d+(-(alpha|beta|rc)\\d+)?-\\d+:(windows|linux|osx)" + Regex.escape(
		"\". Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_9.html#dependency_multi_string_notation"
	) + ".*",
	//"at com.android.build.gradle.internal.res.Aapt2FromMaven\$Companion.create(Aapt2FromMaven.kt:139)",
)

// Gradle 9.1 vs AGP 8.13 https://issuetracker.google.com/issues/444260628
@Suppress("detekt.MaxLineLength")
doNotNagAboutPatternForTest(
	"9.1" to "9.2",
	"8.2" to "8.14", // Lower bound unconfirmed.
	Regex.escape("Declaring dependencies using multi-string notation has been deprecated. This will fail with an error in Gradle 10. Please use single-string notation instead: \"com.android.tools.lint:lint-gradle:") + "\\d+\\.\\d+\\.\\d+(-(alpha|beta|rc)\\d+)?" + Regex.escape(
		"\". Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_9.html#dependency_multi_string_notation"
	) + ".*",
	//"at com.android.build.gradle.internal.lint.LintFromMaven\$Companion.from(AndroidLintInputs.kt:2850)",
)

// Gradle 9.6 vs AGP 9.2 https://issuetracker.google.com/issues/495889752, fixed in AGP 9.3
doNotNagAboutForTest(
	"9.6" to "9.8",
	"9.0" to "9.3",
	// > Configure project :
	// Example test: AndroidBuildPluginIntgTest.`can override compileSdk (debug)`
	"Using a Project object as a dependency notation has been deprecated. " +
			"This will fail with an error in Gradle 10. " +
			"Please use the project(String) method on DependencyHandler or the createProjectDependency(String) method on DependencyFactory instead. " +
			"Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_9.html#dependency_project_notation",
	// VariantDependenciesBuilder.build at :279, :333, :664 can be below Gradle's 10-frame diagnostic limit.
)
