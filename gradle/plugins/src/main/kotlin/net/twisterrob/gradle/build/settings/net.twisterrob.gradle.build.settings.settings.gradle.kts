import net.twisterrob.gradle.build.testing.resetGradleTestWorkerIdToDefault
import net.twisterrob.gradle.nagging.NaggingPlugin

gradle.resetGradleTestWorkerIdToDefault()
plugins.apply(NaggingPlugin::class)
