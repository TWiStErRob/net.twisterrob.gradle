package net.twisterrob.gradle.android

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.ApplicationVariant
import com.android.builder.core.DefaultApiVersion
import com.android.builder.model.ApiVersion
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidBuildPluginExtension {
    boolean decorateBuildConfig = true
    boolean addRunTasks = true
}

public class AndroidBuildPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        BaseExtension android = project.android
        def twisterrob = android.extensions.create('twisterrob', AndroidBuildPluginExtension)

        android.with {
            lintOptions.with {
                xmlReport = false
                checkAllWarnings = true
            }
            buildToolsVersion = "20" // latest Android SDK Build-tools
            compileSdkVersion = "android-19" // Android 4.4.2/SDK Platform

            defaultConfig.with {
                //minSdkVersion = new DefaultApiVersion(10) // 2.3.3 Gingerbread MR1
                targetSdkVersion = new DefaultApiVersion(19) // 4.4.2 KitKat
                buildConfigField "String", "EMAIL", "\"feedback@twisterrob.net\""
            }

            buildTypes['debug'].with {
                applicationIdSuffix = ".debug"
                versionNameSuffix = "-DEBUG"
                buildConfigField "String", "EMAIL", "\"papp.robert.s@gmail.com\""
            }
        }

        project.afterEvaluate {
            def tasks = project.tasks
            if (twisterrob.decorateBuildConfig) {
                tasks.create('decorateBuildConfig', DecorateBuildConfigTask) {
                    description 'Add more information about build to BuildConfig.java'
                    tasks.preBuild.dependsOn delegate
                }

                project.android.applicationVariants.all { variant ->
                    addPackageName(variant)
                }
            }

            if (twisterrob.addRunTasks) {
                project.android.applicationVariants.all { variant ->
                    if (variant.install) {
                        variant.install.project.tasks.create(
                                name: "run${variant.name.capitalize()}",
                                type: AndroidInstallRunner,
                                dependsOn: variant.install
                        ) {
                            delegate.variant = variant
                        }
                    }
                }
            }
        }
    }

    static void addPackageName(ApplicationVariant variant) {
        // Need to have at least one resValue to NOT SKIP generateResValuesTask (i.e. run its doFirst)
        // Since buildType is attached to multiple flavors this value will be overwritten a few times
        // until the doFirst sets the final value, JUST BEFORE use
        variant.buildType.resValue "string", "app_package", "PLACEHOLDER for ${variant.name}"
        // The below should be variant.(add)resValue or variant.mergedFlavor.(add)resValue, but there's not such API,
        // overwriting before every use is a workaround, and it seems to work if there are flavors as well
        variant.variantData.generateResValuesTask.doFirst {
            // Add package for use e.g. in preferences to launch intent from the right package
            variant.buildType.resValue "string", "app_package", "${variant.applicationId}"
        }
    }
}
