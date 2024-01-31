package net.twisterrob.gradle.vcs.git

import org.eclipse.jgit.errors.RepositoryNotFoundException
import org.eclipse.jgit.lib.RepositoryCache
import org.eclipse.jgit.util.FS
import org.gradle.api.provider.ValueSource
import java.io.FileNotFoundException

internal abstract class GitRepoExistsValueSource : ValueSource<Boolean, GitOperationParams> {

	// 'git describe --always'.execute([], project.rootDir).waitFor() == 0
	override fun obtain(): Boolean {
		val gitDir = parameters.gitDirFile
		// Check more than just the presence of .git to lessen the possibility of detecting "git",
		// but not actually having a git repository.
		RepositoryCache.FileKey.resolve(gitDir, FS.DETECTED) ?: return false
		return try {
			// Actually try to open the repository now.
			inRepo(gitDir) { /* Just open, then close. */ }
			true
		} catch (_: FileNotFoundException) {
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=572617
			false
		} catch (_: RepositoryNotFoundException) {
			false
		}
	}
}
