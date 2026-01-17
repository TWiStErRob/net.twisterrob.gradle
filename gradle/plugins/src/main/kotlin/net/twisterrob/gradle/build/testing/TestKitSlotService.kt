package net.twisterrob.gradle.build.testing

import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import java.util.BitSet
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Allocates small integer "slots" for Gradle TestKit directories so they can be reused across tasks and builds.
 *
 * Slots are tracked per task path (owner) so they can be released immediately after each task finishes.
 */
abstract class TestKitSlotService : BuildService<BuildServiceParameters.None>, AutoCloseable {

	private val lock = ReentrantLock()
	private val leased = BitSet() // true = taken
	private val leasesByOwner = mutableMapOf<String, MutableSet<Int>>()

	/**
	 * Lease a slot id (1-based) and associate it with an owner key (e.g. task path).
	 * Reuses freed ids first.
	 */
	fun lease(owner: String): Int = lock.withLock {
		val index0 = leased.nextClearBit(0)
		leased.set(index0)
		val id = index0 + 1
		leasesByOwner.getOrPut(owner) { mutableSetOf() }.add(id)
		id
	}

	/** Release all slots leased by the given owner (e.g. a task). */
	fun releaseAll(owner: String) {
		lock.withLock {
			val ids = leasesByOwner.remove(owner).orEmpty()
			ids.forEach { leased.clear(it - 1) }
		}
	}

	override fun close() {
		// nothing to cleanup; service is scoped to the build.
	}
}

