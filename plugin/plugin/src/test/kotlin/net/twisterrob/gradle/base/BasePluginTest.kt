package net.twisterrob.gradle.base

import org.gradle.api.ProjectConfigurationException
import org.gradle.util.GradleVersion
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Test

// TODO Parameterize
class BasePluginTest {

	@Test fun `fails with very old version`() {
		assertThrows<ProjectConfigurationException> {
			BasePlugin.checkGradleVersion(GradleVersion.version("3.5.1"))
		}
	}

	@Test fun `allows newer version`() {
		BasePlugin.checkGradleVersion(GradleVersion.version("5.0"))
	}

	@Test fun `fails for incompatible version`() {
		assertThrows<ProjectConfigurationException> {
			BasePlugin.checkGradleVersion(GradleVersion.version("4.0"))
		}
	}

	@Test fun `passes compatible version`() {
		BasePlugin.checkGradleVersion(GradleVersion.version("4.1"))
	}

	@Test fun `passes compatible patched version`() {
		BasePlugin.checkGradleVersion(GradleVersion.version("4.5.1"))
	}

	@Test fun `passes new compatible version`() {
		BasePlugin.checkGradleVersion(GradleVersion.version("4.6"))
	}
}
