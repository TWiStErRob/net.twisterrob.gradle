/**
 * A set of methods useful for stating assumptions about the conditions in which a test is meaningful.
 * A failed assumption does not mean the code is broken, but that the test provides no useful information.
 * The default JUnit runner treats tests with failing assumptions as ignored. Custom runners may behave differently.
 *
 * For example:
 * ```
 * // only provides information if database is reachable.
 * @Test fun calculateTotalSalary() {
 *     val dbc = Database.connect()
 *     assumeNotNull(dbc)
 *     // ...
 * }
 * ```
 *
 * These methods can be used directly: `Assumptions.assumeTrue(...)`,
 * however, they read better if they are referenced through static import:
 * ```
 * import static org.junit.jupiter.api.Assumptions.*;
 * ...
 * assumeTrue(...);
 * ```
 */
@file:JvmName("MatcherAssume")

package org.hamcrest

import org.junit.jupiter.api.Assumptions

/**
 * Call to assume that [actual] satisfies the condition specified by [matcher].
 * If not, the test halts and is ignored.
 * Example:
 * ```
 * assumeThat(1, equalTo(1)); // passes
 * foo(); // will execute
 * assumeThat(0, equalTo(1)); // assumption failure! test halts
 * int x = 1 / 0; // will never execute
 * ```
 *
 * @param T the static type accepted by the matcher
 * (This can flag obvious compile-time problems such as `assumeThat(1, is("a")`.)
 * @param actual the computed value being compared
 * @param matcher an expression, built of [Matcher]s, specifying allowed values
 *
 * @see org.hamcrest.Matcher
 * @see org.hamcrest.Matchers
 */
fun <T> assumeThat(actual: T, matcher: Matcher<in T>) {
	assumeThat("", actual, matcher)
}

/**
 * Call to assume that `actual` satisfies the condition specified by `matcher`.
 * If not, the test halts and is ignored.
 * Example:
 * <pre>:
 * assumeThat(1, is(1)); // passes
 * foo(); // will execute
 * assumeThat(0, is(1)); // assumption failure! test halts
 * int x = 1 / 0; // will never execute
</pre> *
 *
 * @param <T> the static type accepted by the matcher
 * (This can flag obvious compile-time problems such as `assumeThat(1, is("a"))`.)
 * @param message the message to be included in the Exception if the assumption is invalid
 * @param actual the computed value being compared
 * @param matcher an expression, built of [Matcher]s, specifying allowed values
 *
 * @see org.hamcrest.Matcher
 * @see org.hamcrest.Matchers
 */
fun <T> assumeThat(message: String?, actual: T, matcher: Matcher<in T>) {
	Assumptions.assumeTrue(matcher.matches(actual)) {
		val description: Description = StringDescription()
		description.appendText(message)
			.appendText("\nExpected: ")
			.appendDescriptionOf(matcher)
			.appendText("\n     but: ")
		matcher.describeMismatch(actual, description)
		description.toString()
	}
}
