package net.twisterrob.gradle.android

import net.twisterrob.gradle.java.JavaPlugin
import net.twisterrob.gradle.vcs.VCSPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidLibraryPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.apply plugin: com.android.build.gradle.LibraryPlugin

        project.apply plugin: VCSPlugin
        project.apply plugin: JavaPlugin
        project.apply plugin: AndroidBuildPlugin
        //project.apply plugin: AndroidVersionPlugin
        //project.apply plugin: AndroidSigningPlugin
        //project.apply plugin: AndroidProguardPlugin
    }
}
