package net.twisterrob.gradle.vcs;

class NOPPluginExtension implements VCSExtension {
	boolean isAvailable() {
		return false
	}
	String getRevision() {
		return "no VCS"
	}
	int getRevisionNumber() {
		return 0
	}
}
