package net.twisterrob.gradle.vcs

import net.twisterrob.gradle.base.BasePlugin
import net.twisterrob.gradle.kotlin.dsl.extensions
import net.twisterrob.gradle.vcs.VCSPluginExtension.Companion.vcs
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.kotlin.dsl.create
import org.tmatesoft.svn.cli.SVN
import org.tmatesoft.svn.core.SVNException
import org.tmatesoft.svn.core.internal.wc.SVNFileUtil
import org.tmatesoft.svn.core.wc.ISVNOptions
import org.tmatesoft.svn.core.wc.SVNClientManager
import org.tmatesoft.svn.core.wc.SVNStatus
import org.tmatesoft.svn.core.wc.SVNWCUtil
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.security.Permission

class SVNPlugin : BasePlugin() {

	override fun apply(target: Project) {
		super.apply(target)
		project.vcs.extensions.create<SVNPluginExtension>(SVNPluginExtension.NAME, project)
	}
}

open class SVNPluginExtension(
	private val project: Project
) : VCSExtension {

	override val revision: String
		get() = revisionNumber.toString()

	// Same as: new XmlSlurper().parseText(SVN.cli('info', '--xml')).entry.commit.@revision
	override val revisionNumber: Int
		get() = open().revision.number.toInt()

	override val isAvailableQuick: Boolean
		get() = project.rootDir.resolve(SVNFileUtil.getAdminDirectoryName()).exists()

	// Same as 'svn info'.execute([], project.rootDir).waitFor() == 0
	override val isAvailable: Boolean
		get() =
			try {
				open().isVersioned
			} catch (ignore: SVNException) {
				false
			}

	override fun files(project: Project): FileCollection =
		project.files(
			project.rootDir.resolve(".svn/wc.db")
		)

	private fun open(): SVNStatus {
		val options: ISVNOptions = SVNWCUtil.createDefaultOptions(true)
		val clientManager = SVNClientManager.newInstance(options)
		val statusClient = clientManager.statusClient
		return statusClient.doStatus(project.rootDir, false)
	}

	// key method/closure - used as: def out = doSvnMain( 'your', 'svn', 'args', 'go', 'here' );
	@Suppress("unused")
	private fun cli(vararg svnArgs: String): String =
		captureSystemOut {
			preventExit { SVN.main(svnArgs) }
		}

	companion object {

		internal const val NAME: String = "svn"

		@Suppress("DEPRECATION") // TODEL if there's a replacement in https://bugs.openjdk.org/browse/JDK-8199704.
		private inline fun <T> preventExit(block: () -> T): T {
			val oldSecurityManager = System.getSecurityManager()
			System.setSecurityManager(NonExitingSecurityManager) // stop SVN.main from doing a System.exit call
			try {
				return block()
			} finally {
				System.setSecurityManager(oldSecurityManager)
			}
		}

		private inline fun captureSystemOut(block: () -> Unit): String {
			val baos = ByteArrayOutputStream()
			val oldSystemOut = System.out
			System.setOut(PrintStream(baos))
			try {
				block()
				System.out.flush()
				return baos.toString()
			} finally {
				System.setOut(oldSystemOut)
			}
		}
	}

	private object NonExitingSecurityManager : @Suppress("DEPRECATION") SecurityManager() {

		override fun checkPermission(perm: Permission) {
			// Do nothing, allow it.
		}

		override fun checkExit(status: Int) {
			throw SecurityException() // Prevent it.
		}
	}
}
