plugins {
	id("net.twisterrob.gradle.build.module.gradle-plugin")
	id("net.twisterrob.gradle.build.publish")
}

base.archivesName = "twister-convention-versioning"
description = "Versioning Convention Plugin: Gradle Plugin to set up versioning through properties and DSL."

gradlePlugin {
	@Suppress("UnstableApiUsage", "detekt.StringLiteralDuplication")
	plugins {
		create("vcs") {
			id = "net.twisterrob.gradle.plugin.vcs"
			displayName = "Versioning Convention Plugin"
			description = """
				TWiStErRob's Convention plugin for Version Control.
				
				Features:
				 * Auto-detect GIT and SVN version control.
				 * Expose VCS information such as revision and revisionNumber.
				 * Auto-version Android artifacts (versionCode and versionName) from version.properties and VCS.
				 * Rename Android APK to contain more information:
				   `{applicationId}@{versionCode}-v{versionName}+{variant}.apk`
			""".trimIndent()
			tags = setOf("conventions", "android", "versioning", "git", "svn", "vcs")
			implementationClass = "net.twisterrob.gradle.vcs.VCSPlugin"
			deprecateId(project, "net.twisterrob.vcs")
		}
	}
}

dependencies {
	implementation(gradleApi())
	implementation(projects.plugin.base)
	implementation(projects.compat.gradle)
	implementation(projects.compat.agpBase)
	implementation(projects.compat.agp)
	implementation(libs.svnkit)
	implementation(libs.svnkit.cli)
	compileOnly(libs.jgit17)
	compileOnly(libs.android.gradle)

	// This plugin is part of the net.twisterrob.gradle.plugin.android-app plugin, not designed to work on its own.
	runtimeOnly(projects.plugin)

	testImplementation(projects.test.internal)
	testImplementation(testFixtures(projects.plugin.base))
	testInjectedPluginClasspath(libs.android.gradle) {
		version { require(property("net.twisterrob.test.android.pluginVersion").toString()) }
	}

	testFixturesApi(libs.svnkit)
	testFixturesCompileOnlyApi(libs.jgit17)
}

fun HasAttributes.copyAttributesFrom(providers: ProviderFactory, origin: HasAttributes) {
	for (key in origin.attributes.keySet()) {
		@Suppress("UNCHECKED_CAST") // The origin will make sure it's the right type.
		val unsafeKey = key as Attribute<Any>
		this.attributes.attributeProvider(unsafeKey, providers.provider { origin.attributes.getAttribute(key) })
	}
}

fun NamedDomainObjectProvider<Configuration>.createJavaVariant(javaVersion: Int) : Configuration {
	val runtimeElementsJava = configurations.consumable("runtimeElementsJava$javaVersion") {
		val base = get()
		extendsFrom(base)
		copyAttributesFrom(providers, base)
		attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, javaVersion)
	}

	components.named<AdhocComponentWithVariants>("java").configure {
		addVariantsFromConfiguration(runtimeElementsJava.get()) { }
	}

	val runtimeOnlyJava = configurations.resolvable("runtimeOnlyJava$javaVersion") {
		val base = configurations.getByName(get().name.replace("Elements", "Only"))
		extendsFrom(base)
		runtimeElementsJava.get().extendsFrom(this)
		copyAttributesFrom(providers, base)
		attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, javaVersion)
	}
	return runtimeOnlyJava.get()
}

val runtimeOnlyJava11 = configurations.runtimeElements.createJavaVariant(11)
val runtimeOnlyJava17 = configurations.runtimeElements.createJavaVariant(17)

configurations.runtimeElements {
	attributes {
		attribute(Attribute.of("reject", String::class.java), "yes")
	}
}
components.getByName<AdhocComponentWithVariants>("java") {
	withVariantsFromConfiguration(configurations.runtimeElements.get()) { skip() }
}

dependencies {
	runtimeOnlyJava11(libs.jgit11) {
		because("JGit 6.10.0 is the last version to support Java 11. JGit 7.x requires Java 17.")
		versionConstraint.rejectedVersions.add("[7.0,)")
	}
	runtimeOnlyJava17(libs.jgit17) {
		because("JGit 7.x is the first version to require Java 17.")
		versionConstraint.rejectedVersions.add("(,7.0)")
	}
}
