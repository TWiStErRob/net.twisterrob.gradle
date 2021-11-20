package net.twisterrob.gradle.vcs

import org.tmatesoft.svn.core.SVNCommitInfo
import org.tmatesoft.svn.core.SVNURL
import org.tmatesoft.svn.core.wc2.SvnInfo
import org.tmatesoft.svn.core.wc2.SvnOperation
import org.tmatesoft.svn.core.wc2.SvnOperationFactory
import org.tmatesoft.svn.core.wc2.SvnTarget
import java.io.File

fun SvnOperationFactory.doCreateRepository(repoDir: File): SVNURL =
	createRepositoryCreate()
		.apply {
			repositoryRoot = repoDir
		}
		.execute { _, result -> println("Repository created at ${result}") }

fun SvnOperationFactory.doCheckout(repoUrl: SVNURL, rootDir: File): Long =
	createCheckout()
		.apply {
			source = SvnTarget.fromURL(repoUrl)
			setSingleTarget(SvnTarget.fromFile(rootDir))
		}
		.execute { op, result -> println("Checked out revision ${result} from ${op.source}") }

fun SvnOperationFactory.doCommitSingleFile(file: File, message: String): SVNCommitInfo {
	val target = SvnTarget.fromFile(file)
	createScheduleForAddition()
		.apply {
			addTarget(target)
		}
		.execute { op -> "Added ${op.firstTarget}" }
	val commit = createCommit()
		.apply {
			addTarget(target)
			commitMessage = message
		}
		.execute { op, result ->
			println("Committed revision ${result.newRevision}: ${op.commitMessage}")
		}
	doUpdateWorkingCopy(file)
	return commit
}

fun SvnOperationFactory.doInfo(file: File): SvnInfo =
	createGetInfo()
		.apply {
			setSingleTarget(SvnTarget.fromFile(file))
		}
		.run()

fun SvnOperationFactory.doUpdateWorkingCopy(file: File): Long {
	val wcRoot = doInfo(file).wcInfo.wcRoot
	return doUpdate(wcRoot)
}

fun SvnOperationFactory.doUpdate(file: File): Long =
	createUpdate()
		.apply {
			setSingleTarget(SvnTarget.fromFile(file))
		}
		.execute { op, result ->
			println("Updated ${op.targets.single()} to ${result.single()}")
		}
		.single()

inline fun svn(block: SvnOperationFactory.() -> Unit) {
	val factory = SvnOperationFactory()
	try {
		block(factory)
	} finally {
		factory.dispose()
	}
}

inline fun <V : Any, T : SvnOperation<V>> T.execute(block: (operation: T, result: V) -> Unit): V {
	val result = run()
	block(this, result)
	return result
}

inline fun <T : SvnOperation<Void>> T.execute(block: (operation: T) -> Unit) {
	run()
	block(this)
}
