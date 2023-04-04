package net.twisterrob.gradle.build.testing

configurations {
    /**
     * This plugin adds a `testInjectedPluginClasspath` configuration
     * that can be used to inject dependencies into the Gradle TestKit runner.
     *
     * This is necessary, because without some extra dependencies here,
     * the test projects will not be able to see AGP plugins
     * (they're `compileOnly` dependencies of plugins under test).
     *
     * Even though the test projects are set up with
     * ```
     * pluginManagement {
     *    plugins {
     *        id("com.android.library") version "@net.twisterrob.test.android.pluginVersion@"
     *    }
     * }
     * ```
     * or with
     * ```
     * pluginManagement {
     *    resolutionStrategy {
     *        eachPlugin {
     *            when (requested.id.namespace) {
     *                "com.android" ->
     *                    useModule("com.android.tools.build:gradle:@net.twisterrob.test.android.pluginVersion@")
     *            }
     *        }
     *    }
     * }
     * ```
     * the build fails with:
     * ```
     * An exception occurred applying plugin request [id: 'net.twisterrob.gradle.plugin.android-library']
     * > Failed to apply plugin 'net.twisterrob.gradle.plugin.android-library'.
     *    > Plugin with id 'com.android.library' not found.
     * ```
     *
     * Adding an extra dependency that's not published through Gradle Metadata or Maven POM works around this issue.
     *
     * Example test that fails without this:
     * [net.twisterrob.gradle.android.AndroidBuildPluginIntgTest].`can disable buildConfig generation (debug)`
     */
	val testInjectedPluginClasspath by creating {
		isCanBeConsumed = false
		isCanBeResolved = true
	}
	tasks.named<PluginUnderTestMetadata>("pluginUnderTestMetadata") {
		this.pluginClasspath.from(testInjectedPluginClasspath)
	}
}