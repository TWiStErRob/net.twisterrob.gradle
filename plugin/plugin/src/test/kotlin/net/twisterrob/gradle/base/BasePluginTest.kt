package net.twisterrob.gradle.base

import org.gradle.api.ProjectConfigurationException
import org.junit.Test
import kotlin.test.assertFailsWith

// TODO Parameterize
class BasePluginTest {

	@Test fun `fails with very old version`() {
		assertFailsWith<ProjectConfigurationException> {
			BasePlugin.checkGradleVersion("3.5.1")
		}
	}

	@Test fun `fails for newer version`() {
		assertFailsWith<ProjectConfigurationException> {
			BasePlugin.checkGradleVersion("5.0")
		}
	}

	@Test fun `fails for incompatible version`() {
		assertFailsWith<ProjectConfigurationException> {
			BasePlugin.checkGradleVersion("4.0")
		}
	}

	@Test fun `passes compatible version`() {
		BasePlugin.checkGradleVersion("4.1")
	}

	@Test fun `passes compatible patched version`() {
		BasePlugin.checkGradleVersion("4.5.1")
	}

	@Test fun `passes new compatible version`() {
		BasePlugin.checkGradleVersion("4.6")
	}
}
