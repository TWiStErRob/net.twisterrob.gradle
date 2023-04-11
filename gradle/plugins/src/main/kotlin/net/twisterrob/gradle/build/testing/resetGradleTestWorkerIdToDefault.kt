package net.twisterrob.gradle.build.testing

import org.gradle.api.internal.tasks.testing.worker.TestWorker
import org.gradle.api.invocation.Gradle
import org.gradle.internal.id.LongIdGenerator
import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.process.internal.worker.DefaultWorkerProcessBuilder
import org.gradle.process.internal.worker.DefaultWorkerProcessFactory
import org.gradle.process.internal.worker.WorkerProcessFactory
import org.gradle.process.internal.worker.child.SystemApplicationClassLoaderWorker
import java.util.concurrent.atomic.AtomicLong

/**
 * [net.twisterrob.gradle.test.GradleTestKitDirRelocator] works really nice,
 * but it keeps creating new and new folders when it's not necessary.
 *
 * This is because a Gradle daemon stays alive and keeps the previous workerId as state.
 * [DefaultWorkerProcessBuilder.build] calls `idGenerator.generateId()` which will propagate,
 * through some funky encoder/decoder serialization,
 * to [SystemApplicationClassLoaderWorker.ContextImpl.getWorkerId].
 * This value will be stored in [TestWorker.WORKER_ID_SYS_PROPERTY] system property.
 *
 * This method will reset the counter to the default to restart counting.
 * It is recommended to call this once at the beginning of the configuration phase.
 * The best place for this is the rootProject's build.gradle file.
 */
fun Gradle.resetGradleTestWorkerIdToDefault() {
	// This factory is "static" and it holds the state we need to mutate in order to make the workers reuse IDs.
	val factory: DefaultWorkerProcessFactory = serviceOf<WorkerProcessFactory>() as DefaultWorkerProcessFactory
	val idGenerator: LongIdGenerator = factory.getPrivateField("idGenerator")
	val nextId: AtomicLong = idGenerator.getPrivateField("nextId")
	nextId.set(1)
}

@Suppress("UNCHECKED_CAST")
private fun <T> Any.getPrivateField(fieldName: String): T =
	this::class.java.getDeclaredField(fieldName).apply { isAccessible = true }.get(this) as T
