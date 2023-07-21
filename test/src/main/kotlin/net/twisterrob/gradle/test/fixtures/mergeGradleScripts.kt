package net.twisterrob.gradle.test.fixtures

internal fun mergeGradleScripts(script1: String, script2: String): String {
	val blocks1 = splitBlocks(script1)
	val blocks2 = splitBlocks(script2)
	val blocks = listOfNotNull(
		blocks1.imports,
		blocks2.imports,
		blocks1.buildscript,
		blocks2.buildscript,
		blocks1.plugins,
		blocks2.plugins,
		blocks1.imperativeCode,
		blocks2.imperativeCode
	)
	return blocks.joinToString(separator = "\n")
}

private fun splitBlocks(script: String): Blocks {
	val importsRegex =
		Regex("""(?sm)(.*?)((?:^import\s+[^\r\n]+?\r?\n)*^import\s+[^\r\n]+?)\r?\n(.*)""")

	fun blockRegex(name: String): Regex =
		@Suppress("RegExpRedundantEscape")
		Regex("""(?sm)(.*?)(^${name}\s*\{\s*?.*?\s*?\r?\n\})(?:\r?\n(?!${name}\s*\{)|\Z)(.*)""")

	val normalizedLineEndings = script.prependIndent("")
	val (importsBlock, scriptWithoutImports) =
		extractBlock(importsRegex, normalizedLineEndings)
	val (buildscriptBlock, scriptWithoutBuildscript) =
		extractBlock(blockRegex("buildscript"), scriptWithoutImports)
	val (pluginsBlock, scriptWithoutBuildscriptAndPlugins) =
		extractBlock(blockRegex("plugins"), scriptWithoutBuildscript)
	return Blocks(
		imports = importsBlock,
		buildscript = buildscriptBlock,
		plugins = pluginsBlock,
		imperativeCode = scriptWithoutBuildscriptAndPlugins.takeIf { it.isNotBlank() }
	)
}

private fun extractBlock(regex: Regex, script: String): Pair<String?, String> {
	val match = regex.find(script)
	val block = match?.let { it.groups[2]?.value }
	val removed = if (match != null) {
		regex.replace(script, "$1$3")
	} else {
		script
	}
	return block to removed
}

private data class Blocks(
	val imports: String?,
	val buildscript: String?,
	val plugins: String?,
	val imperativeCode: String?
)
