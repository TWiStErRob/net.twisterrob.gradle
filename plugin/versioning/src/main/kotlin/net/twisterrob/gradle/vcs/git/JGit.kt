package net.twisterrob.gradle.vcs.git

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.AbbreviatedObjectId
import org.eclipse.jgit.lib.AnyObjectId
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.revwalk.RevWalk
import java.io.File

internal inline fun <T> inRepo(dir: File, block: Git.() -> T): T {
	val repo = Git.open(dir)
	return repo.use(block)
}

internal val Git.head: ObjectId
	get() = this.repository.resolve("HEAD")

internal inline fun <T> Git.walk(block: RevWalk.() -> T): T {
	val walk = RevWalk(this.repository)
	return walk.use(block)
}

internal fun Git.abbreviate(objectId: AnyObjectId): AbbreviatedObjectId =
	this.repository.newObjectReader().use { reader ->
		reader.abbreviate(objectId)
	}
