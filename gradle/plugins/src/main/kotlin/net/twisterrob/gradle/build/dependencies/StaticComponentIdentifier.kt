package net.twisterrob.gradle.build.dependencies

import org.gradle.api.artifacts.component.ComponentIdentifier

/**
 * Based on [org.gradle.internal.component.local.model.OpaqueComponentIdentifier] in Gradle 5.6.4.
 */
class StaticComponentIdentifier(private val displayName: String) : ComponentIdentifier {

	override fun getDisplayName(): String =
		displayName

	override fun equals(other: Any?): Boolean =
		when {
			this === other -> true
			other != null && this::class == other::class -> {
				val that = other as StaticComponentIdentifier
				this.displayName == that.displayName
			}
			else -> false
		}

	override fun hashCode(): Int =
		displayName.hashCode()

	override fun toString(): String =
		displayName
}
