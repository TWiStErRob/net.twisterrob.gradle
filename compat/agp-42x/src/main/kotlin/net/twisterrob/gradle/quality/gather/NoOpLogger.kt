package net.twisterrob.gradle.quality.gather

import se.bjurr.violations.lib.ViolationsLogger
import java.util.logging.Level

class NoOpLogger : ViolationsLogger {
	override fun log(level: Level?, string: String?) {
		// Don't log anything.
	}

	override fun log(level: Level?, string: String?, t: Throwable?) {
		// Don't log anything.
	}
}
