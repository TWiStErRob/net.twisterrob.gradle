package net.twisterrob.gradle.build.publishing

import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.HasAttributes
import org.gradle.api.attributes.java.TargetJvmVersion
import org.gradle.api.component.AdhocComponentWithVariants
import org.gradle.api.provider.ProviderFactory
import org.gradle.jvm.component.internal.JvmSoftwareComponentInternal
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.named

fun Project.createJavaVariant(
	forConfiguration: NamedDomainObjectProvider<Configuration>,
	version: JavaLanguageVersion,
): Configuration {
	skipConfiguration(forConfiguration)
	val javaVersion = version.asInt()
	val runtimeElementsJava = configurations.consumable("${forConfiguration.name}Java$javaVersion") {
		val base = forConfiguration.get()
		extendsFrom(base)
		copyAttributesFrom(providers, base, base.attributes.keySet() - ignore)
		attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, javaVersion)
		components.named<JvmSoftwareComponentInternal>("java").configure {
			mainFeature.apiElementsConfiguration.artifacts.forEach(outgoing.artifacts::add)
		}
	}

	components.named<AdhocComponentWithVariants>("java").configure {
		addVariantsFromConfiguration(runtimeElementsJava.get()) { }
	}

	val onlyName = forConfiguration.name.replace("Elements", "Only")
	val runtimeOnlyJava = configurations.resolvable("${onlyName}Java$javaVersion") {
		val base = configurations.getByName(onlyName)
		extendsFrom(base)
		runtimeElementsJava.get().extendsFrom(this)
		copyAttributesFrom(providers, base, base.attributes.keySet() - ignore)
		attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, javaVersion)
	}
	return runtimeOnlyJava.get()
}

private fun HasAttributes.copyAttributesFrom(
	providers: ProviderFactory,
	origin: HasAttributes,
	keys: Set<Attribute<*>> = origin.attributes.keySet(),
) {
	for (key in keys) {
		@Suppress("UNCHECKED_CAST") // The origin will make sure it's the right type.
		val unsafeKey = key as Attribute<Any>
		this.attributes.attributeProvider(unsafeKey, providers.provider { origin.attributes.getAttribute(key) })
	}
}

private val ignore = Attribute.of("ignore", String::class.java)

private fun Project.skipConfiguration(configuration: NamedDomainObjectProvider<Configuration>) {
	configuration.configure {
		attributes {
			attribute(ignore, "yes")
		}
	}
	this.components.getByName<AdhocComponentWithVariants>("java") {
		withVariantsFromConfiguration(configuration.get()) { skip() }
	}
}
