import org.gradle.api.file.FileCollection
import org.gradle.api.initialization.dsl.VersionCatalogBuilder
import java.io.File

/**
 * Hack to be able to load multiple files into a [org.gradle.api.artifacts.VersionCatalog].
 * The built-in method is [VersionCatalogBuilder.from] which only allows one file.
 * 
 * This calls the internal function behind [VersionCatalogBuilder.from] skipping the checks.
 *
 * TODEL https://github.com/gradle/gradle/issues/20383#issuecomment-1236419331
 */
fun VersionCatalogBuilder.load(file: File) {
	org.gradle.api.internal.catalog.parser.TomlCatalogFileParser.parse(file.inputStream(), this)
}

fun VersionCatalogBuilder.load(files: FileCollection) {
	files.forEach { load(it) }
}
