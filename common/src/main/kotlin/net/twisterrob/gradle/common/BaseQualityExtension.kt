package net.twisterrob.gradle.common

import org.gradle.api.Action
import org.gradle.api.internal.file.pattern.PatternMatcherFactory
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.util.PatternSet

@Suppress("UnnecessaryAbstractClass") // Gradle convention.
abstract class BaseQualityExtension<T>(
	internal var taskConfigurator: Action<TaskConfigurator<T>> = Action {}
) where T : SourceTask {

	fun taskConfigurator(closure: Action<TaskConfigurator<T>>) {
		taskConfigurator = closure
	}
}

class TaskConfigurator<out T>(val task: T) where T : SourceTask {
	/**
	 * @param basePrefix Directory pattern to exclude, no trailing slash
	 * @param relativeExceptions relative to `basePrefix`, no trailing slash
	 */
	fun excludeExcept(basePrefix: String, vararg relativeExceptions: String) {
		val exactException = PatternMatcherFactory.compile(true, "${basePrefix}/*")
		val exceptions = PatternSet()
			.include("${basePrefix}/*")
			.exclude(relativeExceptions.map { "${basePrefix}/${it}" })
		task.source.matching(exceptions).visit { file ->
			if (file.isDirectory && exactException.matches(file.relativePath.segments, 0)) {
				task.exclude(file.relativePath.pathString + "/**")
			}
		}
	}
}
