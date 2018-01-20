package net.twisterrob.gradle.common

import com.android.SdkConstants
import com.android.build.gradle.tasks.LintBaseTask
import groovy.transform.CompileDynamic
import org.gradle.api.Task

import java.util.function.BinaryOperator
import java.util.function.Function
import java.util.stream.Collector
import java.util.stream.Collectors

final class Utils {

	private Utils() {
	}

	@CompileDynamic
	static <T> T safeAdd(T a, T b) {
		if (a != null && b != null) {
			return a + b
		} else if (a != null && b == null) {
			return a
		} else if (a == null && b != null) {
			return b
		} else /* (a == null && b == null) */ {
			return null
		}
	}

	static Collector<Integer, ?, Integer> nullSafeSum() {
		return nullSafeSum(Function.<Integer> identity())
	}

	/**
	 * @param mapper T is @Nullable
	 */
	static <T> Collector<T, ?, Integer> nullSafeSum(Function<T, Integer> mapper) {
		Collectors.reducing((Integer)null, mapper, Utils.&safeAdd as BinaryOperator<Integer>)
	}

	static boolean wasExplicitlyLaunched(Task task) {
		return task.project.gradle.startParameter.taskNames == [ task.path ]
	}

	static File getXmlOutput(LintBaseTask task) {
		def xmlOutput = task.lintOptions.xmlOutput
		if (xmlOutput != null) {
			return xmlOutput
		}
		// method reference helps to call private method without explicit reflection
		//noinspection UnnecessaryQualifiedReference internal API, be explicit
		def createOutputPath = com.android.build.gradle.internal.dsl.LintOptions.&createOutputPath
		return createOutputPath(
				task.project,
				task.variantName,
				SdkConstants.DOT_XML,
				task.reportsDir,
				task.isFatalOnly()
		) as File
	}
}
