package net.twisterrob.gradle.test.testkit

import org.gradle.internal.Factory
import org.gradle.internal.service.DefaultServiceRegistry
import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.gradle.tooling.internal.consumer.ConnectorServices
import org.gradle.tooling.internal.consumer.DefaultGradleConnector
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever
import java.io.File

/**
 * Tried to do it by setting JAVA_HOME, but that didn't work, because that would be interpreted by `gradlew`, which doesn't play when running tests using Gradle Test Kit.
 * ```
 * runner.withEnvironment(mapOf("JAVA_HOME" to System.getenv("JAVA_HOME").toString()))
 * ```
 * Found a method to set the Java Home in the Gradle Test Kit, but that's not publicly delegated.
 * Using reflection to intercept a local variable is the only way to set the Java Home.
 *
 * @see org.gradle.testkit.runner.internal.ToolingApiGradleExecutor.run
 * @see org.gradle.tooling.internal.consumer.DefaultBuildLauncher.setJavaHome
 */
internal fun setJavaHome(javaHome: File) {
	BuildLauncherInterceptor {
		setJavaHome(javaHome)
	}.configure()
}

private class BuildLauncherInterceptor(private val configuration: BuildLauncher.() -> BuildLauncher) {

	fun configure() {
		// Need to call launcher.* on a local variable in ToolingApiGradleExecutor.run.
		// - launcher is created by connection.newBuild() implemented by DefaultProjectConnection
		// - connection is created by gradleConnector.connect() implemented by DefaultGradleConnector
		// - gradleConnector is created by GradleConnector.newConnector() delegate to ConnectorServices.createConnector()
		// - createConnector uses ConnectorServiceRegistry for dependency injection
		// This last one is static, so we can intercept the registry to intercept the launcher.
		singletonRegistry = intercept(singletonRegistry)
	}

	private fun intercept(registry: DefaultServiceRegistry): DefaultServiceRegistry =
		// Tried spying on ConnectorServices\$ConnectorServiceRegistry.createConnectorFactory directly, but it didn't work.
		spy(registry).apply {
			doAnswer { @Suppress("UNCHECKED_CAST") intercept(it.callRealMethod() as Factory<GradleConnector>) }
				.whenever(this)
				.getFactory(DefaultGradleConnector::class.java)
		}

	private fun intercept(factory: Factory<GradleConnector>): Factory<GradleConnector> =
		spy(factory).apply {
			doAnswer { intercept(it.callRealMethod() as GradleConnector) }
				.whenever(this)
				.create()
		}

	private fun intercept(connector: GradleConnector): GradleConnector =
		spy(connector).apply {
			doAnswer { intercept(it.callRealMethod() as ProjectConnection) }
				.whenever(this)
				.connect()
		}

	private fun intercept(connection: ProjectConnection): ProjectConnection =
		spy(connection).apply {
			doAnswer { intercept(it.callRealMethod() as BuildLauncher) }
				.whenever(this)
				.newBuild()
		}

	private fun intercept(launcher: BuildLauncher): BuildLauncher =
		launcher.run(configuration)
}

private var singletonRegistry: DefaultServiceRegistry
	get() = ConnectorServices::class.java
		.getDeclaredField("singletonRegistry")
		.apply { isAccessible = true }
		.get(null) as DefaultServiceRegistry
	set(value) {
		ConnectorServices::class.java
			.getDeclaredField("singletonRegistry")
			.apply { isAccessible = true }
			.set(null, value)
	}
