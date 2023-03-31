package net.twisterrob.gradle.build.compilation

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
		context.details.withVariant("runtimeElements") {
			val version = context.details.id.version
			if (version.startsWith("7.4.") || version.startsWith("30.4.")) {
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
