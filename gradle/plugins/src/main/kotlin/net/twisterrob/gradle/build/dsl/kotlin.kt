package net.twisterrob.gradle.build.dsl

import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.SourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

/**
 * @see <a href="https://github.com/JetBrains/kotlin/blob/v1.2.20/buildSrc/src/main/kotlin/sourceSets.kt#L45-L54">sourceSets.kt</a>
 * @see <a href="https://github.com/gradle/kotlin-dsl/issues/343#issuecomment-331636906">DSL</a>
 * @see <a href="https://youtrack.jetbrains.com/issue/KT-47047">Replacement not ready</a>
 */
@Suppress("DEPRECATION")
val SourceSet.kotlin: SourceDirectorySet
	get() =
		(this as org.gradle.api.internal.HasConvention)
			.convention
			.getPlugin(KotlinSourceSet::class.java)
			.kotlin

/**
 * @see <a href="https://github.com/JetBrains/kotlin/blob/v1.2.20/buildSrc/src/main/kotlin/sourceSets.kt#L45-L54">sourceSets.kt</a>
 */
fun SourceSet.kotlin(action: SourceDirectorySet.() -> Unit) {
	kotlin.action()
}
