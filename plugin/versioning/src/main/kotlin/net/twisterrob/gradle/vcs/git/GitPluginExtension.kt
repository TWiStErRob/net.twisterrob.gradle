package net.twisterrob.gradle.vcs.git

import net.twisterrob.gradle.ext.forUseAtConfigurationTimeCompat
import net.twisterrob.gradle.vcs.VCSExtension
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.provider.ValueSource
import javax.inject.Inject

@Suppress("detekt.UnnecessaryAbstractClass") // Gradle convention.
abstract class GitPluginExtension(
	private val rootDir: Directory
) : VCSExtension {

	@get:Inject
	internal abstract val providers: ProviderFactory

	override val isAvailableQuick: Boolean
		get() = rootDir.dir(".git").asFile.exists()

	override val isAvailable: Boolean
		get() = read<GitRepoExistsValueSource, Boolean>()

	override val revision: String
		get() = read<GitRevisionValueSource, String>()

	override val revisionNumber: Int
		get() = read<GitRevisionNumberValueSource, Int>()

	override fun files(project: Project): FileCollection =
		project.files(
			".git/HEAD",
			// Delay File operations to when the FileCollection is resolved.
			project.provider {
				val headRef = project.rootDir.resolve(".git/HEAD")
				if (headRef.exists() && headRef.isFile && headRef.canRead()) {
					val headRaw = headRef.readText().trimEnd()
					if (headRaw.startsWith("ref: ")) {
						// HEAD contains a ref, resolve it to a file containing the SHA as the input.
						project.rootDir.resolve(".git").resolve(headRaw.substringAfter("ref: "))
					} else {
						// HEAD contains an SHA, that's the input.
						headRef
					}
				} else {
					error("Cannot find ${headRef}, consider android.twisterrob.decorateBuildConfig = false")
				}
			}
		)

	private inline fun <reified T : ValueSource<R, GitOperationParams>, R: Any> read(): R =
		providers
			.of(T::class.java) { it.parameters.gitDir.set(rootDir) }
			.forUseAtConfigurationTimeCompat()
			.get()

	companion object {

		internal const val NAME: String = "git"
	}
}
