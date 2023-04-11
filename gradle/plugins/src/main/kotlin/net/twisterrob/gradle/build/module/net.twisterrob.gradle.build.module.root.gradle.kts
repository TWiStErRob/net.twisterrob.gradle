import net.twisterrob.gradle.build.detekt.DetektRootPlugin
import net.twisterrob.gradle.build.dsl.libs

plugins.apply(DetektRootPlugin::class)

val target: String = project.libs.versions.kotlin.target.get()
val language: String = project.libs.versions.kotlin.language.get()
check(target.startsWith(language)) {
	error("Kotlin target version ($target) must be compatible with language version ($language).")
}
