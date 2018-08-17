package net.twisterrob.gradle.vcs

import net.twisterrob.gradle.base.BaseExposedPlugin
import org.gradle.api.Project

class VCSPluginExtension implements VCSExtension {
	VCSExtension current //= new NOPPluginExtension();
//	SVNPlugin svn
//	GITPlugin git

	boolean isAvailableQuick() {
		return current.isAvailableQuick()
	}

	boolean isAvailable() {
		return current.isAvailable()
	}

	String getRevision() {
		return current.getRevision()
	}

	int getRevisionNumber() {
		return current.getRevisionNumber()
	}
}

class VCSPlugin extends BaseExposedPlugin {
	static VCSExtension whichVCS(VCSPluginExtension container) {
		if (container.current) {
			return container.current
		} else if (container.svn.isAvailableQuick()) {
			return container.svn
		} else if (container.git.isAvailableQuick()) {
			return container.git
		} else if (container.svn.isAvailable()) {
			return container.svn
		} else if (container.git.isAvailable()) {
			return container.git
		}
		return new NOPPluginExtension()
	}

	void apply(Project target) {
		super.apply(target)

		project.extensions.create("VCS", VCSPluginExtension)
		project.apply plugin: SVNPlugin
		project.apply plugin: GITPlugin
		project.VCS.current = whichVCS((VCSPluginExtension)target.VCS)
	}
}
