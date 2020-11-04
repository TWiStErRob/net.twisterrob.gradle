package net.twisterrob.gradle.vcs

import net.twisterrob.gradle.base.BasePlugin
import net.twisterrob.gradle.kotlin.dsl.extensions
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByName
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

		val svn = project
			.extensions.getByName<VCSPluginExtension>(VCSPluginExtension.NAME)
			.extensions.create<SVNPluginExtension>(SVNPluginExtension.NAME)
		svn.project = project // TODO better solution
	}
}

open class SVNPluginExtension : VCSExtension {

	companion object {
		internal const val NAME = "svn"
	}

	internal lateinit var project: Project

	private fun open(): SVNStatus {
		val options: ISVNOptions = SVNWCUtil.createDefaultOptions(true)
		val clientManager = SVNClientManager.newInstance(options)
		val statusClient = clientManager.statusClient
		return statusClient.doStatus(project.rootDir, false)
	}

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

	// key method/closure - used as: def out = doSvnMain( 'your', 'svn', 'args', 'go', 'here' );
	@Suppress("unused")
	private fun cli(vararg svnArgs: String): String {
		System.setSecurityManager(NonExitingSecurityManager) // stop SVN.main from doing a System.exit call
		val baos = ByteArrayOutputStream()
		val oldSystemOut = System.out
		System.setOut(PrintStream(baos))
		try {
			SVN.main(svnArgs)
			System.out.flush()
			return baos.toString()
		} finally {
			System.setOut(oldSystemOut)
			System.setSecurityManager(null)
		}
	}

	private object NonExitingSecurityManager : SecurityManager() {
		override fun checkPermission(perm: Permission) {
			// do nothing
		}

		override fun checkExit(status: Int) =
			throw SecurityException()
	}
}
