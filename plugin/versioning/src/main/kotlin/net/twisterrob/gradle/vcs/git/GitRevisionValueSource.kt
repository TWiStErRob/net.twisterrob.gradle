package net.twisterrob.gradle.vcs.git

import org.gradle.api.provider.ValueSource

internal abstract class GitRevisionValueSource : ValueSource<String, GitOperationParams> {

	// 'git rev-parse --short HEAD'.execute([], project.rootDir).text.trim()
	override fun obtain(): String =
		inRepo(parameters.gitDirFile) {
			abbreviate(head).name()
		}
}
