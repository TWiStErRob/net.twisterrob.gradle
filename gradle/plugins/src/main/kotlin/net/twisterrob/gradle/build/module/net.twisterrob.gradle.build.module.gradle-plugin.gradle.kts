import net.twisterrob.gradle.build.compilation.JavaCompatibilityPlugin
import net.twisterrob.gradle.build.detekt.DetektPlugin
import net.twisterrob.gradle.build.testing.InitScriptMetadataPlugin
import org.gradle.kotlin.dsl.apply

project.plugins.apply("org.gradle.java-gradle-plugin")
project.plugins.apply("net.twisterrob.gradle.build.testing.plugin-under-test-metadata-extras")
project.plugins.apply(InitScriptMetadataPlugin::class)

project.plugins.apply("org.jetbrains.kotlin.jvm")
project.plugins.apply(JavaCompatibilityPlugin::class)
//project.plugins.apply("org.gradle.java-test-fixtures")

project.plugins.apply(DetektPlugin::class)
