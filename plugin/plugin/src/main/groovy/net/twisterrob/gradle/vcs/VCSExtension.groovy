package net.twisterrob.gradle.vcs

interface VCSExtension {
	boolean isAvailable()
	boolean isAvailableQuick()
	String getRevision()
	int getRevisionNumber()
}
