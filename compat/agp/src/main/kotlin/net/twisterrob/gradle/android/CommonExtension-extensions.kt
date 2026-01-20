package net.twisterrob.gradle.android

import com.android.build.api.dsl.CommonExtension
import net.twisterrob.gradle.common.AGPVersions
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtensionContainer

/**
 * [CommonExtension] only started inheriting [ExtensionAware] directly from AGP 9.0.
 * Before that it was inheriting it through Gradle's `_Decorated` magic.
 * Casing to [ExtensionAware] helps the Java runtime to reconcile this and prevent:
 * > java.lang.NoSuchMethodError:
 * > 'org.gradle.api.plugins.ExtensionContainer com.android.build.api.dsl.CommonExtension.getExtensions()'
 */
val CommonExtension.extensionsCompat: ExtensionContainer
	get() = when {
		AGPVersions.v9xx <= AGPVersions.CLASSPATH -> this.extensions
		else -> (this as ExtensionAware).extensions
	}
