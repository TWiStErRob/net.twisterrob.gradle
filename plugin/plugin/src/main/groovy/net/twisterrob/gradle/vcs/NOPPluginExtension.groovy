package net.twisterrob.gradle.vcs

class NOPPluginExtension implements VCSExtension {
	boolean isAvailableQuick() {
		return false
	}

	boolean isAvailable() {
		return isAvailableQuick()
	}

	String getRevision() {
		return "no VCS"
	}
	int getRevisionNumber() {
		return 0
	}
}
