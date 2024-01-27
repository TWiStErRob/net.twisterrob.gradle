package net.twisterrob.gradle.logging

import org.gradle.api.logging.LogLevel
import java.util.Properties

internal class RedirectParser {
	fun parse(properties: Properties): Map<String, LogLevelRedirect> =
		properties
			.stringPropertyNames()
			.filter { it.startsWith("net.twisterrob.gradle.logging.redirect.") }
			.associate { prop ->
				val classOrPackage = prop.substringAfter("net.twisterrob.gradle.logging.redirect.")
				val levelMapping = toLogLevelRedirect(properties.getProperty(prop))
				classOrPackage to levelMapping
			}

	private fun toLogLevelRedirect(levelMapping: String): LogLevelRedirect {
		val mappings = levelMapping
			.split(",")
			.map { mapping ->
				mapping.split("->")
					.also { require(it.size == 2) { "Invalid mapping: ${levelMapping}" } }
			}
			.flatMap @Suppress("detekt.UseIfInsteadOfWhen") { split ->
				val froms = when (val fromRaw = split[0].trim()) {
					"TRACE" -> arrayOf<LogLevel?>(null)
					"*" -> @Suppress("UNCHECKED_CAST") (LogLevel.values() as Array<LogLevel?> + null)
					else -> arrayOf(LogLevel.valueOf(fromRaw))
				}
				val to = when (val toRaw = split[1].trim()) {
					"TRACE" -> null
					else -> LogLevel.valueOf(toRaw)
				}
				froms.map { it to to }
			}
			.toMap()
		return { mappings.getOrDefault(it, it) }
	}
}
