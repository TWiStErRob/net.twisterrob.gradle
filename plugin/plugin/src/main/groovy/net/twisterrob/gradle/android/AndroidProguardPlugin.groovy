package net.twisterrob.gradle.android

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.BaseVariant
import com.android.builder.core.DefaultBuildType
import com.android.builder.model.AndroidProject
import net.twisterrob.gradle.Utils
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidProguardPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        BaseExtension android = project.android
        File myProguardRules = new File("${project.buildDir}/${AndroidProject.FD_INTERMEDIATES}/proguard/twisterrob.pro")
        android.with {
            DefaultBuildType release = buildTypes.release
            release.runProguard = true
            release.proguardFiles.add android.getDefaultProguardFile('proguard-android.txt')
            release.proguardFiles.add myProguardRules
        }

        project.task('extractProguardRules') {
            description = "Extract proguard file from 'net.twisterrob.android' plugin"
            outputs.file(myProguardRules)
            doLast {
                new FileOutputStream(myProguardRules).withStream { outFile ->
                    AndroidProguardPlugin.classLoader.getResourceAsStream("twisterrob.pro").withStream { inFile ->
                        outFile << inFile
                    }
                }
            }
        }

        project.afterEvaluate {
            Utils.getVariants(android).all { BaseVariant variant ->
                if (variant.obfuscation) {
                    variant.obfuscation.dependsOn variant.obfuscation.project.tasks.extractProguardRules
                }
            }
        }
    }
}
