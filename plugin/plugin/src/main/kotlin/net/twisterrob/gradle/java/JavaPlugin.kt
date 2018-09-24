package net.twisterrob.gradle.java

import com.android.build.gradle.BaseExtension
import com.android.builder.core.VariantType.ANDROID_TEST
import com.android.builder.core.VariantType.UNIT_TEST
import net.twisterrob.gradle.Utils
import net.twisterrob.gradle.base.BaseExposedPluginForKotlin
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getPlugin
import org.gradle.kotlin.dsl.withType

private val DEFAULT_JAVA_VERSION = JavaVersion.VERSION_1_7
private val DEFAULT_JAVA_TEST_VERSION = JavaVersion.VERSION_1_8
private const val DEFAULT_ENCODING = "UTF-8"

class JavaPlugin : BaseExposedPluginForKotlin() {

	override fun apply(target: Project) {
		super.apply(target)

		if (Utils.hasAndroid(project)) {
			val android: BaseExtension = project.extensions["android"] as BaseExtension
			with(android.compileOptions) {
				encoding = DEFAULT_ENCODING
				setDefaultJavaVersion(DEFAULT_JAVA_VERSION)
				setSourceCompatibility(DEFAULT_JAVA_VERSION)
				setTargetCompatibility(DEFAULT_JAVA_VERSION)
			}
		} else {
			project.plugins.apply("java")
		}

		if (project.plugins.hasPlugin(org.gradle.api.plugins.JavaPlugin::class.java)) {
			with(project.convention.getPlugin<JavaPluginConvention>()) {
				sourceCompatibility = DEFAULT_JAVA_VERSION
				targetCompatibility = DEFAULT_JAVA_VERSION

				sourceSets["main"].compileClasspath += project.configurations.maybeCreate("provided")
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
				changeCompatibility(DEFAULT_JAVA_TEST_VERSION)
			}

			val compileVersion = JavaVersion.toVersion(sourceCompatibility)
			if (compileVersion < JavaVersion.current()) {
				// prevent :compileJava warning: [options] bootstrap class path not set in conjunction with -source 1.x
				fixClasspath(compileVersion)
			}
			if (isTestTask && isAndroidUnitTest) {
				doFirst {
					if (isTestTask && !isAndroidTest) {
						// TODO hacky, need to reapply at doFirst, because otherwise it resets as if it was production code
						changeCompatibility(DEFAULT_JAVA_TEST_VERSION)
					}
					classpath += project.files(options.bootstrapClasspath)
					fixClasspath(JavaVersion.toVersion(sourceCompatibility))
				}
			}
		}
	}
}

private fun JavaCompile.fixClasspath(compileVersion: JavaVersion) {
	val envVar = "JAVA${compileVersion.majorVersion}_HOME"
	val root = System.getenv(envVar)
	var rt = project.file("$root/jre/lib/rt.jar")
	if (!rt.exists()) {
		rt = project.file("$root/lib/rt.jar")
	}
	if (!rt.exists()) {
		logger.warn(
			"Java Compatibility: javac needs a bootclasspath, "
					+ "but no jre/lib/rt.jar or lib/rt.jar found in $envVar (=$root)."
		)
		return
	}
	logger.info("Java Compatiblity: using rt.jar from $rt")
	options.bootstrapClasspath = project.files(rt.absolutePath)
}

private fun JavaCompile.changeCompatibility(ver: JavaVersion) {
	val origS = sourceCompatibility
	val origT = targetCompatibility
	sourceCompatibility = ver.toString()
	targetCompatibility = ver.toString()
	logger.info("Changed compatibility ${origS}/${origT} to ${ver}/${ver}")
}
