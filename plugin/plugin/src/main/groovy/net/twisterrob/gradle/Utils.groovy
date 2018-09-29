package net.twisterrob.gradle

import org.gradle.api.plugins.ExtensionContainer

class Utils {

	/**
	 * Call {@code extensions} property on the object dynamically.
	 */
	static ExtensionContainer getExtensions(Object obj) {
		return obj.extensions
	}
}
