package net.twisterrob.gradle.vcs

import net.twisterrob.gradle.common.BaseExposedPlugin
import org.gradle.api.Project

class VCSPluginExtension implements VCSExtension {
	VCSExtension current

	@Override
	boolean isAvailable() {
		return current.isAvailable()
	}

	@Override
	String getRevision() {
		return current.getRevision()
	}

	@Override
	int getRevisionNumber() {
		return current.getRevisionNumber()
	}
}

class VCSPlugin extends BaseExposedPlugin {
	static VCSExtension whichVCS(container) {
		if (container.svn.isAvailable()) {
			return container.svn
		} else if (container.git.isAvailable()) {
			return container.git
		}
		return new NOPPluginExtension();
	}

	void apply(Project target) {
		super.apply(target)

		project.extensions.create("VCS", VCSPluginExtension)
		project.apply plugin: SVNPlugin
		project.apply plugin: GITPlugin
		project.VCS.current = whichVCS(target.VCS)
	}
}
