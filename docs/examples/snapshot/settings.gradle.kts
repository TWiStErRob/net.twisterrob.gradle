import net.twisterrob.gradle.doNotNagAbout

pluginManagement {
	repositories {
		mavenCentral()
		//gradlePluginPortal() // not used
		exclusiveContent {
			forRepository {
				google()
			}
			filter {
				includeGroupByRegex("""^com\.android(\..*)?$""")
				includeGroupByRegex("""^com\.google\.android\..*$""")
				includeGroupByRegex("""^com\.google\.testing\.platform$""")
				includeGroupByRegex("""^androidx\..*$""")
			}
		}
		exclusiveContent {
			forRepository {
				maven("https://s01.oss.sonatype.org/content/repositories/snapshots/") {
					name = "Sonatype 01"
					mavenContent {
						snapshotsOnly()
					}
				}
			}
			filter {
				includeGroupByRegex("""^net\.twisterrob\.gradle\.plugin\.[^.]+$""")
				includeGroupByRegex("""^net\.twisterrob\.gradle$""")
			}
		}
	}
}

plugins {
	id("net.twisterrob.gradle.plugin.settings") version "0.19-SNAPSHOT"
	id("net.twisterrob.gradle.plugin.nagging") version "0.19-SNAPSHOT"
}

dependencyResolutionManagement {
	repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
	repositories {
		google()
		mavenCentral()
	}
}

if (!JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_17)) {
	error("Java 17+ is required to build this project, found: ${JavaVersion.current()}.")
}

val gradleVersion: String = GradleVersion.current().version

// TODEL Gradle 8.13 vs AGP 8.0-8.9 https://issuetracker.google.com/issues/370546370
@Suppress("detekt.MaxLineLength")
doNotNagAbout(
	"Declaring an 'is-' property with a Boolean type has been deprecated. " +
			"Starting with Gradle 9.0, this property will be ignored by Gradle. " +
			"The combination of method name and return type is not consistent with Java Bean property rules and will become unsupported in future versions of Groovy. " +
			"Add a method named 'getCrunchPngs' with the same behavior and mark the old one with @Deprecated, " +
			"or change the type of 'com.android.build.gradle.internal.dsl.BuildType\$AgpDecorated.isCrunchPngs' (and the setter) to 'boolean'. " +
			"Consult the upgrading guide for further information: " +
			"https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#groovy_boolean_properties"
)
@Suppress("detekt.MaxLineLength")
doNotNagAbout(
	"Declaring an 'is-' property with a Boolean type has been deprecated. " +
			"Starting with Gradle 9.0, this property will be ignored by Gradle. " +
			"The combination of method name and return type is not consistent with Java Bean property rules and will become unsupported in future versions of Groovy. " +
			"Add a method named 'getUseProguard' with the same behavior and mark the old one with @Deprecated, " +
			"or change the type of 'com.android.build.gradle.internal.dsl.BuildType.isUseProguard' (and the setter) to 'boolean'. " +
			"Consult the upgrading guide for further information: " +
			"https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#groovy_boolean_properties"
)
@Suppress("detekt.MaxLineLength")
doNotNagAbout(
	"Declaring an 'is-' property with a Boolean type has been deprecated. " +
			"Starting with Gradle 9.0, this property will be ignored by Gradle. " +
			"The combination of method name and return type is not consistent with Java Bean property rules and will become unsupported in future versions of Groovy. " +
			"Add a method named 'getWearAppUnbundled' with the same behavior and mark the old one with @Deprecated, " +
			"or change the type of 'com.android.build.api.variant.impl.ApplicationVariantImpl.isWearAppUnbundled' (and the setter) to 'boolean'. " +
			"Consult the upgrading guide for further information: " +
			"https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#groovy_boolean_properties"
)
