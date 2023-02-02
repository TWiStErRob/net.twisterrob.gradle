import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

// TODEL https://github.com/gradle/gradle/issues/15383, see build.gradle.kts
// For some reason can't use libs:
// > Type org.gradle.accessors.dm.LibrariesForLibs not present
@Deprecated("Read comment", replaceWith = ReplaceWith("this.versionCatalogs.named(\"libs\")"))
internal val Project.libs: LibrariesForLibs
	get() = extensions.getByType()
