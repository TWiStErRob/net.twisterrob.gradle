package net.twisterrob.gradle.quality.report

import net.twisterrob.gradle.common.grouper.Grouper
import net.twisterrob.gradle.quality.Violations

internal class ResultGenerator {

	fun build(violations: Grouper.Start<Violations>): String? {
		val result = violations
			.list
			.flatMap { it.violations.orEmpty() }
			.map { violation ->
				val message = violation.message.replace("""(\r?\n)+""".toRegex(), System.lineSeparator())
				val loc = violation.location
				@Suppress("detekt.StringShouldBeRawString") // Not using raw string literals because of the new-line requirements.
				return@map (""
						+ "\n${loc.file.absolutePath}:${loc.startLine} in ${loc.module.path}/${loc.variant}"
						+ "\n\t${violation.source.reporter}/${violation.rule}"
						+ "\n${message.prependIndent("\t")}"
						)
			}
		return if (result.isNotEmpty()) {
			result.joinToString(
				separator = System.lineSeparator() + System.lineSeparator(),
				postfix = System.lineSeparator()
			)
		} else {
			null
		}
	}
}
