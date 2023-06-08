package net.twisterrob.gradle.graph.vis

import org.gradle.cache.PersistentCache
import java.io.Closeable
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.nio.channels.OverlappingFileLockException
import java.util.Properties

abstract class VisualizerSettings<Settings : Any> protected constructor(
	private val cache: PersistentCache
) : Closeable {

	private val settingsFileName: String
		get() = this::class.java.name + ".properties"

	var settings: Settings
		get() = try {
			cache.useCache<Settings> {
				val propsFile = File(cache.baseDir, settingsFileName)
				val props = Properties()
				try {
					props.load(FileReader(propsFile))
					readSettings(props)
				} catch (ignore: FileNotFoundException) {
					readSettings(Properties()) // First startup.
				} catch (ex: IOException) {
					throw IllegalStateException("Cannot read settings from ${propsFile}", ex)
				}
			}
		} catch (ex: OverlappingFileLockException) {
			System.err.println("Cannot read settings, using defaults: $ex") // TODO logging
			createDefault()
		}
		set(settings) {
			try {
				val props = writeSettings(settings)
				cache.useCache {
					val propsFile = File(cache.baseDir, settingsFileName)
					try {
						props.store(FileWriter(propsFile), null)
					} catch (ex: IOException) {
						throw IllegalStateException("Cannot save settings to ${propsFile}", ex)
					}
				}
			} catch (ex: OverlappingFileLockException) {
				System.err.println("Cannot save settings: $ex") // TODO logging
			}
		}

	protected abstract fun readSettings(props: Properties): Settings
	protected abstract fun writeSettings(settings: Settings): Properties
	protected abstract fun createDefault(): Settings

	override fun close() {
		cache.close()
	}
}
