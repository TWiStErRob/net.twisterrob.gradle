package net.twisterrob.gradle.java

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import net.twisterrob.gradle.common.BaseExposedPlugin
import org.gradle.api.JavaVersion
import org.gradle.api.Project
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
		}
	}
}
