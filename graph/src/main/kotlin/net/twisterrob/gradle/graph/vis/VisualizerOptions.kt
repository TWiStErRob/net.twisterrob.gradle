package net.twisterrob.gradle.graph.vis

import net.twisterrob.gradle.graph.logger
import org.gradle.cache.PersistentCache
import java.io.Closeable
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.nio.channels.OverlappingFileLockException
import java.util.Properties

abstract class VisualizerOptions<Options : Any> protected constructor(
	private val cache: PersistentCache
) : Closeable {

	@Suppress("PrivatePropertyName", "VariableNaming") // Keep conventional name.
	private val LOG = logger(this)

	private val storageFileName: String
		get() = this::class.java.name + ".properties"

	var options: Options
		get() = try {
			cache.useCache<Options> {
				val propsFile = File(cache.baseDir, storageFileName)
				val props = Properties()
				try {
					props.load(FileReader(propsFile))
					readOptions(props)
				} catch (ignore: FileNotFoundException) {
					readOptions(Properties()) // First startup.
				} catch (ex: IOException) {
					throw IllegalStateException("Cannot read options from ${propsFile}", ex)
				}
			}
		} catch (ex: OverlappingFileLockException) {
			LOG.error("Cannot read options, using defaults.", ex)
			createDefault()
		}
		set(value) {
			try {
				val props = writeOptions(value)
				cache.useCache {
					val propsFile = File(cache.baseDir, storageFileName)
					try {
						props.store(FileWriter(propsFile), null)
					} catch (ex: IOException) {
						throw IllegalStateException("Cannot save options to ${propsFile}", ex)
					}
				}
			} catch (ex: OverlappingFileLockException) {
				LOG.error("Cannot save options.", ex)
			}
		}

	protected abstract fun readOptions(props: Properties): Options
	protected abstract fun writeOptions(options: Options): Properties
	protected abstract fun createDefault(): Options

	override fun close() {
		cache.close()
	}
}
