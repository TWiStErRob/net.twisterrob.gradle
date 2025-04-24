import java.lang.management.ManagementFactory
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion
import org.gradle.kotlin.dsl.support.serviceOf

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
			${PrintDiagnostics.memoryDiagnostics()}
			Running `gradle ${gradleTasks.joinToString(" ")}`
			on ${gradleBuildFile.absolutePath}.
		""".trimIndent()
	)
}

@Suppress("UnstableApiUsage")
class PrintDiagnostics : FlowAction<FlowParameters.None> {
	override fun execute(ignore: FlowParameters.None) {
		println(memoryDiagnostics())
	}
	companion object {
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
	}
}

@Suppress("UnstableApiUsage")
serviceOf<FlowScope>().always(PrintDiagnostics::class.java) { }
