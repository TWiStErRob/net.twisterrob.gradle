package net.twisterrob.gradle.build.dependencies

import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.FileCollectionDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.internal.artifacts.dependencies.DefaultSelfResolvingDependency
import org.gradle.api.internal.artifacts.dsl.dependencies.DependencyFactoryInternal
import org.gradle.api.internal.file.FileCollectionInternal

// STOPSHIP why is this used so many times if there's a replaceGPADWK?
fun DependencyHandler.gradleApiWithoutKotlin(): Dependency =
	withoutKotlin(DependencyFactoryInternal.ClassPathNotation.GRADLE_API)

fun DependencyHandler.gradleKotlinDslWithoutKotlin(): Dependency =
	withoutKotlin(DependencyFactoryInternal.ClassPathNotation.GRADLE_KOTLIN_DSL)

fun DependencyHandler.gradleTestKitWithoutKotlin(): Dependency =
	withoutKotlin(DependencyFactoryInternal.ClassPathNotation.GRADLE_TEST_KIT)

private fun DependencyHandler.withoutKotlin(notation: DependencyFactoryInternal.ClassPathNotation): Dependency {
	// Originally created in org.gradle.api.internal.notations.DependencyClassPathNotationConverter.create
	val gradleApi = create(notation) as FileCollectionDependency
	val filteredSource = gradleApi.files.filter { !it.name.startsWith("kotlin-") }
	val displayName = StaticComponentIdentifier("${notation.displayName} (without Kotlin)")
	return DefaultSelfResolvingDependency(displayName, filteredSource as FileCollectionInternal)
}
