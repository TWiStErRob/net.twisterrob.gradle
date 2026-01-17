import net.twisterrob.gradle.build.testing.resetWorkerIdToDefault
import net.twisterrob.gradle.nagging.NaggingPlugin

gradle.resetWorkerIdToDefault()

// Cannot use plugins { id("net.twisterrob.gradle.plugin.nagging") }, Gradle says "not found".
plugins.apply(NaggingPlugin::class)
