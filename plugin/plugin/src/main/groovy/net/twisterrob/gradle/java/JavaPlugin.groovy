package net.twisterrob.gradle.java

import com.android.build.gradle.*
import com.android.builder.core.VariantType
import net.twisterrob.gradle.common.BaseExposedPlugin
import org.gradle.api.*
import org.gradle.api.tasks.compile.JavaCompile

class JavaPlugin extends BaseExposedPlugin {
	public static final JavaVersion DEFAULT_JAVA_VERSION = JavaVersion.VERSION_1_7
	public static final JavaVersion DEFAULT_JAVA_TEST_VERSION = JavaVersion.VERSION_1_8
	public static final String DEFAULT_ENCODING = 'UTF-8'

	@Override
	void apply(Project target) {
		super.apply(target)

		if (project.plugins.hasPlugin(AppPlugin) || project.plugins.hasPlugin(LibraryPlugin)) {
			TestedExtension android = project.android
			android.compileOptions.encoding = DEFAULT_ENCODING
			android.compileOptions.defaultJavaVersion = DEFAULT_JAVA_VERSION
			android.compileOptions.sourceCompatibility = DEFAULT_JAVA_VERSION
			android.compileOptions.targetCompatibility = DEFAULT_JAVA_VERSION
		} else {
			project.apply plugin: 'java'
		}

		if (project.plugins.hasPlugin(org.gradle.api.plugins.JavaPlugin)) {
			project.sourceCompatibility = DEFAULT_JAVA_VERSION
			project.targetCompatibility = DEFAULT_JAVA_VERSION

			project.sourceSets['main'].compileClasspath += project.configurations.maybeCreate('provided')
		}

		project.tasks.withType(JavaCompile) { compiler ->
			compiler.options.encoding = DEFAULT_ENCODING
			def isTestTask = compiler.name.contains('Test')
			def isAndroidTest = compiler.name.endsWith(VariantType.ANDROID_TEST.suffix + 'JavaWithJavac')
			def isAndroidUnitTest = compiler.name.endsWith(VariantType.UNIT_TEST.suffix + 'JavaWithJavac')
			if (!isTestTask) {
				compiler.options.compilerArgs << '-Xlint:unchecked' << '-Xlint:deprecation'
			}
			if (isTestTask && !isAndroidTest) {
				changeCompatibility(compiler, DEFAULT_JAVA_TEST_VERSION)
			}

			def compileVersion = JavaVersion.toVersion(compiler.sourceCompatibility)
			if (compileVersion < JavaVersion.current()) {
				// prevent :compileJava warning: [options] bootstrap class path not set in conjunction with -source 1.x
				fixClasspath(compiler, compileVersion)
			}
			if (isTestTask && isAndroidUnitTest) {
				compiler.doFirst {
					// TODO hacky, need to reapply at doFirst, because otherwise it resets as if it was production code
					changeCompatibility(compiler, DEFAULT_JAVA_TEST_VERSION)
					compiler.classpath += project.files(compiler.options.bootClasspath)
					fixClasspath(compiler, JavaVersion.toVersion(compiler.sourceCompatibility))
				}
			}
		}
	}
	private void fixClasspath(JavaCompile compiler, JavaVersion compileVersion) {
		def envVar = "JAVA${compileVersion.majorVersion}_HOME"
		def root = System.env[envVar]
		def rt = project.file("$root/jre/lib/rt.jar")
		if (!rt.exists()) {
			rt = project.file("$root/lib/rt.jar")
		}
		if (!rt.exists()) {
			compiler.logger.warn("Java Compatibility: javac needs a bootclasspath, " +
					"but no jre/lib/rt.jar or lib/rt.jar found in $envVar (=$root).")
			return
		}
		compiler.logger.info("Java Compatiblity: using rt.jar from $rt")
		compiler.options.bootClasspath = rt.absolutePath
	}
	private static void changeCompatibility(JavaCompile task, JavaVersion ver) {
		def origS = task.sourceCompatibility
		def origT = task.targetCompatibility
		task.sourceCompatibility = ver.toString()
		task.targetCompatibility = ver.toString()
		task.logger.info("Changed compatibility ${origS}/${origT} to ${ver}/${ver}")
	}
}
