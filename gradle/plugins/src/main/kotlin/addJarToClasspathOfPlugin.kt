import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.named
import org.gradle.plugin.devel.tasks.PluginUnderTestMetadata

fun Project.addJarToClasspathOfPlugin() {
	tasks.named<PluginUnderTestMetadata>("pluginUnderTestMetadata") { // extract
		// In Gradle 6.5.1 to 6.6 upgrade something changed.
		// The folders on the classpath
		// classpath files('net.twisterrob.gradle\\plugin\\plugin\\build\\classes\\java\\main')
		// classpath files('net.twisterrob.gradle\\plugin\\plugin\\build\\classes\\kotlin\\main')
		// classpath files('net.twisterrob.gradle\\plugin\\plugin\\build\\resources\\main')
		// are now used as a quickly ZIPped JAR file
		// file:/Temp/.gradle-test-kit-TWiStEr-6/caches/jars-8/612d2cded1e3015b824ce72a63bd2fb6/main.jar
		// but this is missing the MANIFEST.MF file, as only the class and resource files are there.
		// Adding the temporary directory for the manifest is not enough like this:
		// it.pluginClasspath.from(files(file("build/tmp/jar/")))
		// because it needs to be in the same JAR file as the class files.
		// To work around this: prepend the final JAR file on the classpath:
		val jar = tasks.named<Jar>("jar").get()
		pluginClasspath.setFrom(files(jar.archiveFile) + pluginClasspath)

		// TODO check if org.gradle.api.plugins.JavaBasePlugin.COMPILE_CLASSPATH_PACKAGING_SYSTEM_PROPERTY might help.
	}
}
