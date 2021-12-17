package net.twisterrob.gradle.java

import com.android.build.gradle.BaseExtension
import com.android.builder.core.VariantTypeImpl.ANDROID_TEST
import com.android.builder.core.VariantTypeImpl.UNIT_TEST
import net.twisterrob.gradle.android.hasAndroid
import net.twisterrob.gradle.base.BaseExposedPlugin
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.getPlugin
import org.gradle.kotlin.dsl.withType
import org.gradle.util.GradleVersion

private const val DEFAULT_ENCODING = "UTF-8"

abstract class BaseJavaPlugin : BaseExposedPlugin() {

	abstract fun applyDefaultPlugin()

	override fun apply(target: Project) {
		super.apply(target)

		if (project.plugins.hasAndroid()) {
			val android: BaseExtension = project.extensions["android"] as BaseExtension
			with(android.compileOptions) {
				encoding = DEFAULT_ENCODING
				setDefaultJavaVersion(JavaVersion.VERSION_1_7)
				setSourceCompatibility(JavaVersion.VERSION_1_7)
				setTargetCompatibility(JavaVersion.VERSION_1_8)
			}
		} else {
			applyDefaultPlugin()
		}

		if (project.plugins.hasPlugin(org.gradle.api.plugins.JavaPlugin::class.java)) {
			when {
				GradleVersion.current().baseVersion < GradleVersion.version("7.1") -> {
					@Suppress("DEPRECATION")
					with(project.convention.getPlugin<org.gradle.api.plugins.JavaPluginConvention>()) {
						sourceCompatibility = JavaVersion.VERSION_1_7
						targetCompatibility = JavaVersion.VERSION_1_8

						sourceSets["main"].compileClasspath += project.configurations.maybeCreate("provided")
					}
				}
				else -> {
					with(project.extensions.getByName<JavaPluginExtension>("java")) {
						sourceCompatibility = JavaVersion.VERSION_1_7
						targetCompatibility = JavaVersion.VERSION_1_8

						sourceSets["main"].compileClasspath += project.configurations.maybeCreate("provided")
					}
				}
			}
		}

		project.tasks.withType<JavaCompile> {
			options.encoding = DEFAULT_ENCODING
			val isTestTask = name.contains("Test")
			val isAndroidTest = name.endsWith("${ANDROID_TEST.suffix}JavaWithJavac")
			val isAndroidUnitTest = name.endsWith("${UNIT_TEST.suffix}JavaWithJavac")
			if (!isTestTask) {
				options.compilerArgs.add("-Xlint:unchecked")
				options.compilerArgs.add("-Xlint:deprecation")
			}
			if (isTestTask && !isAndroidTest) {
				changeCompatibility(JavaVersion.VERSION_1_8)
			}

			val compileVersion = JavaVersion.toVersion(sourceCompatibility)
			fixClasspathIfNecessary(compileVersion)
			if (isTestTask && isAndroidUnitTest) {
				doFirst {
					if (isTestTask && !isAndroidTest) {
						// TODO hacky, need to reapply at doFirst, because otherwise it resets as if it was production code
						changeCompatibility(JavaVersion.VERSION_1_8)
					}
					classpath += project.files(options.bootstrapClasspath)
					fixClasspathIfNecessary(JavaVersion.toVersion(sourceCompatibility))
				}
			}

			doFirst { removeDuplicateCompilerArgs() }
		}
	}
}

/**
 * Prevent this warning for compileJava and compileTestJava and others.
 * > :compileJava warning: [options] bootstrap class path not set in conjunction with -source 1.x
 */
private fun JavaCompile.fixClasspathIfNecessary(compileVersion: JavaVersion) {
	if (JavaVersion.current() == compileVersion) {
		// Same version is set as the one running Gradle, nothing to do.
		return
	}
	val envVar = "JAVA${compileVersion.majorVersion}_HOME"
	val root = System.getenv(envVar)
	var rt = project.file("$root/jre/lib/rt.jar")
	if (!rt.exists()) {
		rt = project.file("$root/lib/rt.jar")
	}
	if (!rt.exists()) {
		logger.warn(
			"Java Compatibility: javac needs a bootclasspath, "
					+ "but no jre/lib/rt.jar or lib/rt.jar found in $envVar (=$root).\n"
					+ "Make sure $envVar is set to a distribution of JDK ${compileVersion.majorVersion}."
		)
		return
	}
	logger.info("Java Compatibility: using rt.jar from $rt")
	options.bootstrapClasspath = project.files(rt.absolutePath)
}

private fun JavaCompile.changeCompatibility(ver: JavaVersion) {
	val origS = sourceCompatibility
	val origT = targetCompatibility
	sourceCompatibility = ver.toString()
	targetCompatibility = ver.toString()
	logger.info("Changed compatibility ${origS}/${origT} to ${ver}/${ver}")
}

/**
 * Removes duplicate `-Xlint:opt` parameters from a `javac` task.
 * If something is both disabled and enabled, make the disable win.
 *
 * Examples:
 *  * given: `-Xlint:unchecked -Xlint:-deprecation -Xlint:deprecation`,
 *    result: `-Xlint:unchecked -Xlint:-deprecation`
 *  * given: `-Xlint:unchecked -Xlint:deprecation -Xlint:-deprecation`,
 *    result: `-Xlint:unchecked -Xlint:-deprecation`
 *
 * Usage:
 * ```
 * tasks.withType(JavaCompile).configureEach { doFirst { removeDuplicateCompilerArgs(it) } }
 * ```
 *
 * Since the suggested usage is [org.gradle.api.Task.doFirst],
 * it doesn't matter if it's before or after the relevant `options.compilerArgs += [ ... ]` setup.
 */
private fun JavaCompile.removeDuplicateCompilerArgs() {
	logger.debug("${this} (input): ${options.compilerArgs}")
	val duplicates = options.compilerArgs
		.filter { it.startsWith("-Xlint:-") }
		.map { "-Xlint:${it.substring("-Xlint:-".length)}" }
	options.compilerArgs.removeAll(duplicates)
	logger.debug("${this} (filtered): ${options.compilerArgs}")
}

/**
 * Removes duplicate `-Xlint:opt` parameters from a `javac` task.
 * If something is both disabled and enabled, the last one wins.
 *
 * Examples:
 *  * given: `-Xlint:unchecked -Xlint:-deprecation -Xlint:deprecation`,
 *    result: `-Xlint:unchecked -Xlint:deprecation`
 *  * given: `-Xlint:unchecked -Xlint:deprecation -Xlint:-deprecation`,
 *    result: `-Xlint:unchecked -Xlint:-deprecation`
 *
 * Usage:
 * ```
 * tasks.withType(JavaCompile).configureEach { doFirst { removeDuplicateCompilerArgs2(it) } }
 * ```
 *
 * Since the suggested usage is [org.gradle.api.Task.doFirst],
 * it doesn't matter if it's before or after the relevant `options.compilerArgs += [ ... ]` setup.
 */
private fun JavaCompile.removeDuplicateCompilerArgs2() {
	logger.debug("${this} (input): ${options.compilerArgs}")
	fun xlintName(arg: String): String? =
		when {
			arg.startsWith("-Xlint:-") -> arg.substring("-Xlint:-".length)
			arg.startsWith("-Xlint:") -> arg.substring("-Xlint:".length)
			else -> null
		}

	fun xlintEnabled(arg: String): Boolean? =
		when {
			arg.startsWith("-Xlint:-") -> false
			arg.startsWith("-Xlint:") -> true
			else -> null
		}

	val optionMap = options.compilerArgs
		.filter { xlintName(it) != null }
		.associate { xlintName(it) to xlintEnabled(it) }
	logger.debug("${this} (deduced): $optionMap")
	options.compilerArgs = options.compilerArgs.filter { optionMap[xlintName(it)] == xlintEnabled(it) }
	logger.debug("${this} (filtered): ${options.compilerArgs}")
}
