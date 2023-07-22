package net.twisterrob.gradle.build.compilation

import org.gradle.api.artifacts.CacheableRule
import org.gradle.api.artifacts.ComponentMetadataContext
import org.gradle.api.artifacts.ComponentMetadataRule
import org.gradle.api.attributes.java.TargetJvmVersion
import javax.inject.Inject

@CacheableRule
@Suppress("UnnecessaryAbstractClass") // Gradle convention.
abstract class LatestAgpTargetJvmLoweringRule @Inject constructor(
	private val jvmVersionOverride: Int
) : ComponentMetadataRule {

	override fun execute(context: ComponentMetadataContext) {
		context.details.withVariant("runtimeElements") {
			val version = context.details.id.version
			if (version.startsWith("8.2.") || version.startsWith("31.2.")) {
				attributes {
					val original = getAttribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE)!!
					if (jvmVersionOverride < original) {
						attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, jvmVersionOverride)
					} else {
						error("It is no longer necessary to override to ${jvmVersionOverride} for ${context.details.id}")
					}
				}
			}
		}
	}
}
