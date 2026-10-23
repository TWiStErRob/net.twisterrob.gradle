import net.twisterrob.gradle.nagging.doNotNagAboutForTest
import net.twisterrob.gradle.nagging.doNotNagAboutPatternForTest
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
