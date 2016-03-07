package net.twisterrob.gradle.java

import com.android.build.gradle.*
import net.twisterrob.gradle.common.BaseExposedPlugin
import org.gradle.api.*
import org.gradle.api.tasks.compile.JavaCompile

class JavaPlugin extends BaseExposedPlugin {
	public static final JavaVersion DEFAULT_JAVA_VERSION = JavaVersion.VERSION_1_7

	@Override
	void apply(Project target) {
		super.apply(target)

		if (project.plugins.hasPlugin(AppPlugin) || project.plugins.hasPlugin(LibraryPlugin)) {
			project.android.compileOptions.sourceCompatibility = DEFAULT_JAVA_VERSION
			project.android.compileOptions.targetCompatibility = DEFAULT_JAVA_VERSION
		} else {
			project.apply plugin: 'java'
		}

		if (project.plugins.hasPlugin(org.gradle.api.plugins.JavaPlugin)) {
			project.sourceCompatibility = DEFAULT_JAVA_VERSION
			project.targetCompatibility = DEFAULT_JAVA_VERSION

			project.sourceSets['main'].compileClasspath += project.configurations.maybeCreate('provided')
		}

		project.tasks.withType(JavaCompile) { compiler ->
			if (!compiler.name.contains('Test')) {
				compiler.options.compilerArgs << '-Xlint:unchecked' << '-Xlint:deprecation'
			}
			if (DEFAULT_JAVA_VERSION.compareTo(JavaVersion.current())) {
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
				compiler.options.bootClasspath = rt;
			}
		}
	}
}
