package net.twisterrob.gradle.vcs

import org.ajoberstar.grgit.Grgit
import org.eclipse.jgit.errors.RepositoryNotFoundException
import org.eclipse.jgit.revwalk.RevWalk
import org.gradle.api.*

class GITPlugin implements Plugin<Project> {
	void apply(Project project) {
		def git = project.VCS.extensions.create("git", GITPluginExtension)
		git.project = project // TODO better solution
	}
}

class GITPluginExtension implements VCSExtension {
	Project project

	private Grgit open() {
		return Grgit.open(project.rootDir)
	}
	// 'git describe --always'.execute([], project.rootDir).waitFor() == 0
	boolean isAvailable() {
		try {
			open().close()
			return true
		} catch (RepositoryNotFoundException ignore) {
			return false
		}
	}
	// 'git rev-parse --short HEAD'.execute([], project.rootDir).text.trim()
	String getRevision() {
		def repo = open()
		try {
			return repo.head().abbreviatedId
		} finally {
			repo.close()
		}
	}
	// 'git rev-list --count HEAD'.execute([], project.rootDir).text.trim()
	int getRevisionNumber() {
		def repo = open()
		try {
			def repository = repo.repository.jgit.repository
			def walk = new RevWalk(repository)
			try {
				walk.retainBody = false
				walk.markStart(walk.parseCommit(repository.resolve("HEAD")));
				return walk.iterator().size()
			} finally {
				walk.dispose()
			}
		} finally {
			repo.close()
		}
	}
}
