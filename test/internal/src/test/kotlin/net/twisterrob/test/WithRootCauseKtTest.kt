package net.twisterrob.test

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.opentest4j.AssertionFailedError
import kotlin.test.assertSame

class WithRootCauseKtTest {
	@Test
	fun `cannot set self as root cause`() {
		val ex = Throwable()

		assertThrows<IllegalArgumentException> {
			ex.withRootCause(ex)
		}
	}

	@Test
	fun `can add root cause to simple Throwable`() {
		val ex = Throwable()
		val cause = Throwable()

		val new = ex.withRootCause(cause)

		assertSame(ex, new)
		assertSame(cause, ex.cause)
	}

	@Test
	fun `can add root cause with cause to simple Throwable`() {
		val ex = Throwable()
		val cause1 = Throwable()
		val cause2 = Throwable()
		cause1.initCause(cause2)

		val new = ex.withRootCause(cause1)

		assertSame(ex, new)
		assertSame(cause1, ex.cause)
		assertSame(cause2, cause1.cause)
	}

	@Test
	fun `can add root cause to chain`() {
		val ex1 = Throwable()
		val ex2 = Throwable()
		val ex3 = Throwable()
		ex1.initCause(ex2)
		ex2.initCause(ex3)
		val cause = Throwable()

		val new = ex1.withRootCause(cause)

		assertSame(ex1, new)
		assertSame(ex2, ex1.cause)
		assertSame(ex3, ex2.cause)
		assertSame(cause, ex3.cause)
	}

	@Test
	fun `cannot set long circular chain`() {
		val ex1 = Throwable()
		val ex2 = Throwable()
		val ex3 = Throwable()
		ex1.initCause(ex2)
		ex2.initCause(ex3)

		assertThrows<IllegalArgumentException> {
			ex1.withRootCause(ex1)
		}
	}

	/**
	 * To verify the presence of the problem in
	 * [ota4j-team/opentest4j#5](https://github.com/ota4j-team/opentest4j/issues/5#issuecomment-940474063).
	 */
	@Test
	fun `cannot initCause of AssertionFailedError`() {
		val ex = AssertionFailedError()
		val cause = Throwable()

		assertThrows<IllegalStateException> {
			ex.initCause(cause)
		}
	}

	@Test
	fun `can add root cause to AssertionFailedError`() {
		val ex = AssertionFailedError()
		val cause = Throwable()

		val new = ex.withRootCause(cause)

		assertSame(ex, new)
		assertSame(cause, ex.cause)
	}
}
