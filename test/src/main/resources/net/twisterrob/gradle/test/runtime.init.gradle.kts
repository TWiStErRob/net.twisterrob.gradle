import java.lang.management.ManagementFactory
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion
import org.gradle.api.internal.StartParameterInternal

initscript {
	repositories {
		gradlePluginPortal()
	}
	dependencies {
		classpath("org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:@net.twisterrob.test.kotlin.pluginVersion@")
	}
}

rootProject {
	val gradleTasks = gradle.startParameter.taskNames
	val gradleBuildFile = this.buildFile
	val gradleVersion = gradle.gradleVersion
	val gradleTestWorkerId = System.getProperty("org.gradle.test.worker")
	val gradleHome = gradle.gradleUserHomeDir
	val gradleDir = gradle.gradleHomeDir
	val javaVendor = System.getProperty("java.vendor")
	val javaVersion = System.getProperty("java.version")
	val javaVersionDate = System.getProperty("java.version.date")
	val javaRuntimeName = System.getProperty("java.runtime.name")
	val javaRuntimeVersion = System.getProperty("java.runtime.version")
	val javaHome = System.getProperty("java.home")
	val javaHomeEnv = System.getenv("JAVA_HOME")
	@Suppress("detekt.NullableToStringCall") // Debug info, null is OK.
	val java = "${javaVendor} ${javaRuntimeName} ${javaVersion} (${javaRuntimeVersion} ${javaVersionDate})"
	val kotlinRuntime = KotlinVersion.CURRENT
	val kotlinPlugin = project.getKotlinPluginVersion()

	println( // Using println rather than logger.lifecycle() to make sure it outputs even with --quiet.
		"""
			Gradle ${gradleVersion} worker #${gradleTestWorkerId} at ${gradleHome.absolutePath} with ${gradleDir?.absolutePath}.
			Gradle Java: ${java} from ${javaHome}${if (javaHome != javaHomeEnv) " (JAVA_HOME = ${javaHomeEnv})" else ""}.
			Gradle Kotlin stdlib: ${kotlinRuntime}, Kotlin Gradle Plugin: ${kotlinPlugin}.
			${memoryDiagnostics()}
			Running `gradle ${gradleTasks.joinToString(" ")}`
			on ${gradleBuildFile.absolutePath}.
		""".trimIndent()
	)
}

// gradle.startParameter.isConfigurationCacheRequested is the replacement, but it's only available in Gradle 7.6+.
@Suppress("DEPRECATION")
val isConfigurationCache = (gradle.startParameter as StartParameterInternal).isConfigurationCache()
if (!isConfigurationCache) {
	// TODO deprecated without replacement https://github.com/gradle/gradle/issues/20151
	// Best effort for now as it won't work with configuration cache.
	@Suppress("DEPRECATION")
	gradle.buildFinished { println(memoryDiagnostics()) }
} else {
	// This might be a future solution, but for now this init script is used with Gradle 7.x too,
	// which doesn't have this flow API, only Gradle 8.1+ does.
	// class PrintDiagnostics : FlowAction<FlowParameters.None> {
	// 	override fun execute(ignore: FlowParameters.None) {
	// 		println(memoryDiagnostics())
	// 	}
	// }
	// serviceOf<FlowScope>().always(PrintDiagnostics::class.java) { }
}

fun memoryDiagnostics(): String {
	fun format(max: Long?, used:Long): String {
		fun mb(bytes: Long): String = "${bytes / 1024 / 1024}MB"
		return if (max == null) {
			"${mb(used)} (unlimited)"
		} else {
			"${mb(used)}/${mb(max)} (${mb(max - used)} free)"
		}
	}

	val heap = Runtime.getRuntime()
	heap.gc() // Best effort to get more accurate numbers.
	val heapMax = heap.maxMemory().takeIf { it != Long.MAX_VALUE }
	val heapUsed = heap.totalMemory() - heap.freeMemory()
	val meta = ManagementFactory.getMemoryPoolMXBeans().single { it.name == "Metaspace" }
	val metaMax = meta.usage.max.takeIf { it != -1L }
	val metaUsed = meta.usage.used
	return "Gradle memory: heap = ${format(heapMax, heapUsed)}, metaspace = ${format(metaMax, metaUsed)}."
}
