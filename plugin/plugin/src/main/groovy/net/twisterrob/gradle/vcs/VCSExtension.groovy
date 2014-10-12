package net.twisterrob.gradle.vcs;

interface VCSExtension {
	boolean isAvailable();
	String getRevision();
	int getRevisionNumber();
}
