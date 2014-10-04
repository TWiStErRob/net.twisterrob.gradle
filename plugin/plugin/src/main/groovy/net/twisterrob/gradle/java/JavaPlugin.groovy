package net.twisterrob.gradle.java

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile

class JavaPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        if (project.plugins.hasPlugin(AppPlugin) || project.plugins.hasPlugin(LibraryPlugin)) {
            project.android.compileOptions.sourceCompatibility = JavaVersion.VERSION_1_7
            project.android.compileOptions.targetCompatibility = JavaVersion.VERSION_1_7
        } else {
            project.apply plugin: 'java'
        }

        if (project.plugins.hasPlugin(org.gradle.api.plugins.JavaPlugin)) {
            project.sourceCompatibility = JavaVersion.VERSION_1_7
            project.targetCompatibility = JavaVersion.VERSION_1_7
        }

        project.tasks.withType(JavaCompile) {
            if (!name.contains('Test')) {
                options.compilerArgs << '-Xlint:unchecked' << '-Xlint:deprecation'
            }
        }
    }
}
