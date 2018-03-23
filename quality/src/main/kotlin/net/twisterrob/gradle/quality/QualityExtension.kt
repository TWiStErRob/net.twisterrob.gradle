package net.twisterrob.gradle.quality

import net.twisterrob.gradle.checkstyle.CheckStyleExtension
import net.twisterrob.gradle.pmd.PmdExtension
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware

open class QualityExtension(
		val project: Project
) {

	fun checkstyle(closure: Action<CheckStyleExtension>) {
		closure.execute(extensions.getByType(CheckStyleExtension::class.java))
	}

	fun pmd(closure: Action<PmdExtension>) {
		closure.execute(extensions.getByType(PmdExtension::class.java))
	}

	private val extensions get() = (this as ExtensionAware).extensions
}
