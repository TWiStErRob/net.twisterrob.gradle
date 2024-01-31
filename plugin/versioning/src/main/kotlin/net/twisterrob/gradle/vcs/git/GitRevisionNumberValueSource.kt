package net.twisterrob.gradle.vcs.git

import org.gradle.api.provider.ValueSource

internal abstract class GitRevisionNumberValueSource : ValueSource<Int, GitOperationParams> {

	// 'git rev-list --count HEAD'.execute([], project.rootDir).text.trim()
	override fun obtain(): Int =
		inRepo(parameters.gitDirFile) {
			walk<Int> {
				isRetainBody = false
				markStart(parseCommit(head))
				return count()
			}
		}
}
