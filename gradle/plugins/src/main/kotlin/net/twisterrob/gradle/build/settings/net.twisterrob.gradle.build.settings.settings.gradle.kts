import net.twisterrob.gradle.build.testing.resetGradleTestWorkerIdToDefault
import net.twisterrob.gradle.nagging.NaggingPlugin

gradle.resetGradleTestWorkerIdToDefault()

// Cannot use plugins { id("net.twisterrob.gradle.plugin.nagging") }, Gradle says "not found".
plugins.apply(NaggingPlugin::class)
