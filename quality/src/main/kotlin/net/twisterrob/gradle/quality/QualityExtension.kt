package net.twisterrob.gradle.quality

import net.twisterrob.gradle.checkstyle.CheckStyleExtension
import net.twisterrob.gradle.pmd.PmdExtension
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.kotlin.dsl.getByType

open class QualityExtension(
	val project: Project
) {

	private val extensions: ExtensionContainer
		get() = (this as ExtensionAware).extensions

	fun checkstyle(closure: Action<CheckStyleExtension>) {
		closure.execute(extensions.getByType())
	}

	fun pmd(closure: Action<PmdExtension>) {
		closure.execute(extensions.getByType())
	}
}
