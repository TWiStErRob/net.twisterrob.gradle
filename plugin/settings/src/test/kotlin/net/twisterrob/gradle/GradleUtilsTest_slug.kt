package net.twisterrob.gradle

import net.twisterrob.gradle.test.RootProject
import net.twisterrob.gradle.test.createSubProject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
 
class GradleUtilsTest_slug {
	@Test fun `root module`() {
		val fixtRoot = RootProject()

		val slug = fixtRoot.slug

		assertEquals("root", slug)
	}

	@Test fun `simple submodule`() {
		val fixtModule = RootProject()
			.createSubProject("name")
		assumeTrue(fixtModule.path == ":name")

		val slug = fixtModule.slug

		assertEquals("name", slug)
	}

	@Test fun `nested submodule`() {
		val fixtModule = RootProject()
			.createSubProject("name1")
			.createSubProject("name2")
		assumeTrue(fixtModule.path == ":name1:name2")

		val slug = fixtModule.slug

		assertEquals("name1-name2", slug)
	}

	/**
	 * Example for ambiguity.
	 */
	@Test fun `submodule with dash in the name`() {
		val fixtModule = RootProject()
			.createSubProject("name1-name2")
		assumeTrue(fixtModule.path == ":name1-name2")

		val slug = fixtModule.slug

		assertEquals("name1-name2", slug)
	}

	@Test fun `multiple levels complex module`() {
		val fixtModule = RootProject()
			.createSubProject("name1-name2")
			.createSubProject("name3_name4")
			.createSubProject("name5+name6")
		assumeTrue(fixtModule.path == ":name1-name2:name3_name4:name5+name6")

		val slug = fixtModule.slug

		assertEquals("name1-name2-name3_name4-name5+name6", slug)
	}
}
