package net.twisterrob.gradle.java

import com.android.build.gradle.*
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
			def isAndroidTask = false // TODO figure this out if it causes a problem
			if (!isTestTask) {
				compiler.options.compilerArgs << '-Xlint:unchecked' << '-Xlint:deprecation'
			}
			if (isTestTask && !isAndroidTask) {
				compiler.sourceCompatibility = DEFAULT_JAVA_TEST_VERSION.toString()
				compiler.targetCompatibility = DEFAULT_JAVA_TEST_VERSION.toString()
			}

			if (DEFAULT_JAVA_VERSION < JavaVersion.current()) {
				// prevent :compileJava warning: [options] bootstrap class path not set in conjunction with -source 1.x
				def envVar = "JAVA${DEFAULT_JAVA_VERSION.majorVersion}_HOME"
				def root = System.env[envVar]
				def rt = project.file("$root/jre/lib/rt.jar")
				if (!rt.exists()) {
					rt = project.file("$root/lib/rt.jar")
				}
				if (!rt.exists()) {
					compiler.doFirst {
						logger.warn("Java Compatibility: javac needs a bootclasspath, " +
								"but no jre/lib/rt.jar or lib/rt.jar found in $envVar (=$root).");
					}
					return;
				}
				compiler.doFirst {
					logger.info("Java Compatiblity: using rt.jar from $rt");
				}
				compiler.options.bootClasspath = rt.absolutePath;
			}
		}
	}
}
