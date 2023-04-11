import libs
import net.twisterrob.gradle.build.detekt.DetektRootPlugin

plugins.apply(DetektRootPlugin::class)

val target = project.libs.versions.kotlin.target.get()
val language = project.libs.versions.kotlin.language.get()
check(target.startsWith(language)) {
	error("Kotlin target version ($target) must be compatible with language version ($language).")
}
