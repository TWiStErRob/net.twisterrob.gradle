package net.twisterrob.test

import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.opentest4j.AssertionFailedError

class WithRootCauseKtTest {

	@Nested
	inner class `happy paths` {

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
	}

	@Nested
	inner class `self causation` {

		@Test
		fun `cannot set self as root cause`() {
			val ex = Throwable()

			assertThrows<IllegalArgumentException> {
				ex.withRootCause(ex)
			}
		}

		@Test
		fun `cannot set self swapping chain`() {
			val ex1 = Throwable()
			val ex2 = Throwable()
			ex1.initCause(ex2)

			assertThrows<IllegalArgumentException> {
				ex1.withRootCause(ex1)
			}
		}

		@Test
		fun `cannot set self in long circular chain`() {
			val ex1 = Throwable()
			val ex2 = Throwable()
			val ex3 = Throwable()
			ex1.initCause(ex2)
			ex2.initCause(ex3)

			assertThrows<IllegalArgumentException> {
				ex1.withRootCause(ex1)
			}
		}
	}

	@Nested
	inner class `infinite chains` {

		@Test
		fun `fails fast on infinite swapping chain`() {
			val ex1 = Throwable()
			val ex2 = Throwable()
			ex1.initCause(ex2)
			ex2.initCause(ex1)
			val cause = Throwable()

			assertThrows<IllegalArgumentException> {
				ex1.withRootCause(cause)
			}
		}

		@Test
		fun `fails fast on infinite circular chain`() {
			val ex1 = Throwable()
			val ex2 = Throwable()
			val ex3 = Throwable()
			ex1.initCause(ex2)
			ex2.initCause(ex3)
			ex3.initCause(ex1)
			val cause = Throwable()

			assertThrows<IllegalArgumentException> {
				ex1.withRootCause(cause)
			}
		}
	}

	@Nested
	inner class `regressions validation` {

		/**
		 * To verify the presence of the problem in
		 * [ota4j-team/opentest4j#5](https://github.com/ota4j-team/opentest4j/issues/5#issuecomment-940474063).
		 */
		@Test
		fun `can add root cause to AssertionFailedError`() {
			val ex = AssertionFailedError()
			val cause = Throwable()

			val new = ex.withRootCause(cause)

			assertSame(ex, new)
			assertSame(cause, ex.cause)
		}
	}
}
