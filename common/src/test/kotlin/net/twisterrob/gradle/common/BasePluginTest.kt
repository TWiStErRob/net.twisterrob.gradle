package net.twisterrob.gradle.common

import org.gradle.tooling.BuildException
import org.gradle.util.GradleVersion
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BasePluginTest {

	private fun verifyVersionAllowed(version: String) {
		BasePlugin.checkGradleVersion(GradleVersion.version(version))
	}

	private fun verifyVersionDisallowed(version: String) {
		assertThrows<BuildException> {
			BasePlugin.checkGradleVersion(GradleVersion.version(version))
		}
	}

	@Test fun `fails with very old version`() {
		verifyVersionDisallowed("2.10.1")
		verifyVersionDisallowed("3.5.1")
		verifyVersionDisallowed("3.6")
		verifyVersionDisallowed("4.10.2")
		verifyVersionDisallowed("5.4.1")
		verifyVersionDisallowed("5.6.4")
		verifyVersionDisallowed("6.4.1")
		verifyVersionDisallowed("6.6")
		verifyVersionDisallowed("6.9.2")
		verifyVersionDisallowed("7.6.4")
	}

	@Test fun `fails for incompatible version`() {
		verifyVersionDisallowed("4.0")
		verifyVersionDisallowed("4.0.1")
		verifyVersionDisallowed("4.0-rc-1")
	}

	@Test fun `passes for newer version`() {
		verifyVersionAllowed("8.2")
		verifyVersionAllowed("8.2.1")
		verifyVersionAllowed("8.3")
		verifyVersionAllowed("8.10")
		verifyVersionAllowed("8.10.2")
	}

	@Test fun `passes compatible version`() {
		verifyVersionAllowed("8.1")
		verifyVersionAllowed("8.1.0")
		verifyVersionAllowed("8.1.1")
	}
}
