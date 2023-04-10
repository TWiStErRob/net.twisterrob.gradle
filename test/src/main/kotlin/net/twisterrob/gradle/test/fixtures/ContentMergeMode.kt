package net.twisterrob.gradle.test.fixtures

import java.io.File

enum class ContentMergeMode {
	CREATE {
		override fun merge(into: File, contents: String) {
			require(!into.exists() || into.length() == 0L) {
				"File already exists: ${into.absolutePath}\n${into.readText()}"
			}
			into.writeText(contents)
		}
	},
	OVERWRITE {
		override fun merge(into: File, contents: String) {
			require(into.exists()) { "File does not exist: ${into.absolutePath}" }
			into.writeText(contents)
		}
	},
	APPEND {
		override fun merge(into: File, contents: String) {
			require(into.exists()) { "File does not exist: ${into.absolutePath}" }
			into.appendText(contents)
		}
	},
	PREPEND {
		override fun merge(into: File, contents: String) {
			require(into.exists()) { "File does not exist: ${into.absolutePath}" }
			into.prependText(contents)
		}
	},
	MERGE_GRADLE {
		override fun merge(into: File, contents: String) {
			// This works with both existing and non-existing files.
			val originalContents = if (into.exists()) into.readText() else ""
			into.writeText(mergeGradleScripts(originalContents, contents))
		}
	},
	;

	internal abstract fun merge(into: File, contents: String)
}

private fun File.prependText(text: String) {
	this.writeText(text + this.readText())
}
