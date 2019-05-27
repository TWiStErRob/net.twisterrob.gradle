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
	}

	@Test fun `fails for incompatible version`() {
		verifyVersionDisallowed("4.0")
		verifyVersionDisallowed("4.0.1")
		verifyVersionDisallowed("4.0-rc-1")
	}

	@Test fun `passes for newer version`() {
		verifyVersionAllowed("5.0")
		verifyVersionAllowed("5.4.1")
	}

	@Test fun `passes compatible version`() {
		verifyVersionAllowed("4.1")
		verifyVersionAllowed("4.1-rc-1")
	}

	@Test fun `passes new compatible version`() {
		verifyVersionAllowed("4.6")
		verifyVersionAllowed("4.4")
		verifyVersionAllowed("4.10")
	}

	@Test fun `passes patched version`() {
		verifyVersionAllowed("4.1.0")
		verifyVersionAllowed("4.1.1")
		verifyVersionAllowed("4.4.1")
		verifyVersionAllowed("4.10.3")
	}
}
