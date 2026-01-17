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

	private val lock: ReentrantLock = ReentrantLock()
	private val leased: BitSet = BitSet() // true = taken
	private val slotByOwner: MutableMap<String, Int> = mutableMapOf()

	private val taskSlotByOwner = mutableMapOf<String, Int>()
	private val forkSlotsByOwner = mutableMapOf<String, MutableMap<Long, Int>>()

	/**
	 * Lease a stable slot for a Test task execution (1-based), reused after release.
	 */
	fun leaseTask(owner: String): Int = lock.withLock {
		taskSlotByOwner[owner] ?: run {
			val index0 = leased.nextClearBit(0)
			leased.set(index0)
			val id = index0 + 1
			taskSlotByOwner[owner] = id
			id
		}
	}

	/**
	 * Lease a per-fork slot id for a specific Test task owner.
	 *
	 * @param owner task path
	 * @param forkId a stable id per fork within the build invocation (e.g. org.gradle.test.worker.id)
	 */
	fun leaseFork(owner: String, forkId: Long): Int = lock.withLock {
		val forks = forkSlotsByOwner.getOrPut(owner) { mutableMapOf() }
		forks[forkId] ?: run {
			// Fork slots are local to the task; start from 1.
			val next = (forks.values.maxOrNull() ?: 0) + 1
			forks[forkId] = next
			next
		}
	}

	/** Release all slots leased by the given owner (e.g. a task). */
	fun releaseAll(owner: String) {
		lock.withLock {
			forkSlotsByOwner.remove(owner)
			taskSlotByOwner.remove(owner)?.let { leased.clear(it - 1) }
		}
	}

	@Deprecated("use leaseTask/leaseFork")
	fun lease(owner: String): Int = leaseTask(owner)

	override fun close() {
		// nothing to cleanup; service is scoped to the build.
	}
}
