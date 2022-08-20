package net.twisterrob.gradle.plugins.settings

import org.gradle.api.artifacts.CacheableRule
import org.gradle.api.artifacts.ComponentMetadataContext
import org.gradle.api.artifacts.ComponentMetadataRule
import org.gradle.api.attributes.java.TargetJvmVersion
import javax.inject.Inject

@CacheableRule
abstract class TargetJvmVersionRule @Inject constructor(
	private val jvmVersionOverride: Int
) : ComponentMetadataRule {

	override fun execute(context: ComponentMetadataContext) {
		context.details.allVariants {
			attributes {
				attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, jvmVersionOverride)
			}
		}
	}
}
