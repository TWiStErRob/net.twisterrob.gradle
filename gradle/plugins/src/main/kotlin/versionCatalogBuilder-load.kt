@file:Suppress("MissingPackageDeclaration") // In default package so it "just works" in settings.gradle.kts files.

import org.gradle.api.file.FileCollection
import org.gradle.api.initialization.dsl.VersionCatalogBuilder
import org.gradle.api.problems.Problems
import org.gradle.util.GradleVersion
import java.io.File
import java.io.InputStream
import java.nio.file.Path

// TODO https://github.com/TWiStErRob/net.twisterrob.gradle/issues/432
/**
 * Hack to be able to load multiple files into a [org.gradle.api.artifacts.VersionCatalog].
 * The built-in method is [VersionCatalogBuilder.from] which only allows one file.
 *
 * This calls the internal function behind [VersionCatalogBuilder.from] skipping the checks.
 *
 * TODEL https://github.com/gradle/gradle/issues/20383#issuecomment-1236419331
 */
fun VersionCatalogBuilder.load(file: File) {
	if (GradleVersion.version("8.4") <= GradleVersion.current().baseVersion) {
		// 8.4.0-RC1 https://github.com/gradle/gradle/commit/16bd24c5a1fe94c9ab84103d745536ccaefde0b9
		val problemsProvider = {
			org.gradle.api.internal.catalog.DefaultVersionCatalogBuilder::class.java
				.getDeclaredMethod("getProblemService")
				.invoke(this@load) as Problems
		}
		org.gradle.api.internal.catalog.parser.TomlCatalogFileParser.parse(file.toPath(), this, problemsProvider)
	} else if (GradleVersion.version("8.1") <= GradleVersion.current().baseVersion) {
		// 8.1.0-RC1 https://github.com/gradle/gradle/commit/4c230fd7aeb4425e1ce69be7458df4c435fd0812
		org.gradle.api.internal.catalog.parser.TomlCatalogFileParser::class.java
			.getDeclaredMethod("parse", Path::class.java, VersionCatalogBuilder::class.java)
			.invoke(file.toPath(), this)
	} else if (GradleVersion.version("7.0") <= GradleVersion.current().baseVersion) {
		// 7.0.0-RC1 https://github.com/gradle/gradle/commit/221d143dc7a73ec2dbf28fe2032223418cefcaf3
		org.gradle.api.internal.catalog.parser.TomlCatalogFileParser::class.java
			.getDeclaredMethod("parse", InputStream::class.java, VersionCatalogBuilder::class.java)
			.invoke(file.inputStream(), this)
	} else {
		// There are possibilities for older versions too, but it needs more research and hacks.
		error("Unsupported Gradle version: ${GradleVersion.current()}")
	}
}

fun VersionCatalogBuilder.load(files: FileCollection) {
	files.forEach { load(it) }
}
