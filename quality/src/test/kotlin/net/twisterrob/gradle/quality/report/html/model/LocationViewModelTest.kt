package net.twisterrob.gradle.quality.report.html.model

import com.flextrade.jfixture.JFixture
import net.twisterrob.gradle.quality.Violation
import net.twisterrob.gradle.test.RootProject
import net.twisterrob.gradle.test.createSubProject
import org.gradle.api.Project
import org.gradle.api.Task
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeFalse
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledOnOs
import org.junit.jupiter.api.condition.OS
import org.mockito.kotlin.mock
import java.io.File

class LocationViewModelTest {
	private val fixture = JFixture().apply {
		customise().lazyInstance(Project::class.java, ::RootProject)
		customise().lazyInstance(Task::class.java, ::mock)
	}

	private val rootProject = RootProject()
	private val parentProject = rootProject.createSubProject("sub1")
	private val innerProject = parentProject.createSubProject("sub2")

	@Nested
	inner class `location in file` {
		@Test fun `gets the column from the violation`() {
			val fixtViolation: Violation = fixture.build()

			val sut = LocationViewModel(fixtViolation)

			assertEquals(fixtViolation.location.column, sut.column)
		}

		@Test fun `gets the line from the violation`() {
			val fixtViolation: Violation = fixture.build()

			val sut = LocationViewModel(fixtViolation)

			assertEquals(fixtViolation.location.startLine, sut.startLine)
			assertEquals(fixtViolation.location.endLine, sut.endLine)
		}
	}

	@Nested
	inner class `location in Gradle` {

		@Test fun `gets the variant from the violation`() {
			val fixtViolation: Violation = fixture.build()

			val sut = LocationViewModel(fixtViolation)

			assertEquals(fixtViolation.location.variant, sut.variant)
		}

		@Test fun `gets the path from the violation for root project`() {
			val fixtViolation: Violation = fixture.build {
				location.setField("module", rootProject)
			}

			val sut = LocationViewModel(fixtViolation)

			assertEquals(":", sut.modulePath)
		}

		@Test fun `gets the path from the violation for sub project`() {
			val fixtViolation: Violation = fixture.build {
				location.setField("module", parentProject)
			}

			val sut = LocationViewModel(fixtViolation)

			assertEquals(":sub1", sut.modulePath)
		}

		@Test fun `gets the path from the violation for nested project`() {
			val fixtViolation: Violation = fixture.build {
				location.setField("module", innerProject)
			}

			val sut = LocationViewModel(fixtViolation)

			assertEquals(":sub1:sub2", sut.modulePath)
		}

		@Test fun `gets the module name from the violation for root project`() {
			val fixtViolation: Violation = fixture.build {
				location.setField("module", rootProject)
			}

			val sut = LocationViewModel(fixtViolation)

			assertEquals("", sut.moduleName)
		}

		@Test fun `gets the module name from the violation for sub project`() {
			val fixtViolation: Violation = fixture.build {
				location.setField("module", parentProject)
			}

			val sut = LocationViewModel(fixtViolation)

			assertEquals("sub1", sut.moduleName)
		}

		@Test fun `gets the module name from the violation for nested project`() {
			val fixtViolation: Violation = fixture.build {
				location.setField("module", innerProject)
			}

			val sut = LocationViewModel(fixtViolation)

			assertEquals("sub2", sut.moduleName)
		}

		@Test fun `gets the module prefix from the violation for root project`() {
			val fixtViolation: Violation = fixture.build {
				location.setField("module", rootProject)
			}

			val sut = LocationViewModel(fixtViolation)

			assertEquals("", sut.modulePrefix)
		}

		@Test fun `gets the module prefix from the violation for sub project`() {
			val fixtViolation: Violation = fixture.build {
				location.setField("module", parentProject)
			}

			val sut = LocationViewModel(fixtViolation)

			assertEquals("", sut.modulePrefix)
		}

		@Test fun `gets the module prefix from the violation for nested project`() {
			val fixtViolation: Violation = fixture.build {
				location.setField("module", innerProject)
			}

			val sut = LocationViewModel(fixtViolation)

			assertEquals(":sub1", sut.modulePrefix)
		}
	}

	@Nested
	inner class `absolute location in file system` {

		@Test fun `gets the file name path from the violation`() {
			val fixtViolation: Violation = fixture.build()

			val sut = LocationViewModel(fixtViolation)

			assertEquals(fixtViolation.location.file.name, sut.fileName)
		}

		@Test fun `gets the absolute path from the violation`() {
			val fixtViolation: Violation = fixture.build()

			val sut = LocationViewModel(fixtViolation)

			assertEquals(fixtViolation.location.file.absolutePath, sut.file)
		}

		@Test fun `gets the absolute uri from the violation`() {
			val fixtViolation: Violation = fixture.build {
				location.setField("file", location.file.absoluteFile)
				assertTrue(location.file.isAbsolute)
			}

			val sut = LocationViewModel(fixtViolation)

			assertEquals(fixtViolation.location.file.toURI(), sut.fileAbsoluteAsUrl)
			assertTrue(sut.fileAbsoluteAsUrl.isAbsolute)
		}

		@Test fun `gets the absolute uri from the violation, even when relative`() {
			val fixtViolation: Violation = fixture.build {
				assertFalse(location.file.isAbsolute)
			}

			val sut = LocationViewModel(fixtViolation)

			assertEquals(fixtViolation.location.file.toURI(), sut.fileAbsoluteAsUrl)
			assertTrue(sut.fileAbsoluteAsUrl.isAbsolute)
		}
	}

	@Nested
	inner class `relative location in file system` {
		@Nested
		inner class isLocationExternal {

			@Test fun `file inside the module is internal`() {
				val fixtViolation: Violation = fixture.build {
					location.setField("file", location.module.file("src/main/java/MyClass.java"))
				}

				val sut = LocationViewModel(fixtViolation)

				assertFalse(sut.isLocationExternal)
			}

			@Test fun `file at the module root is internal`() {
				val fixtViolation: Violation = fixture.build {
					location.setField("file", location.module.file("build.gradle"))
				}

				val sut = LocationViewModel(fixtViolation)

				assertFalse(sut.isLocationExternal)
			}

			@Test fun `file in the root module is internal`() {
				val fixtViolation: Violation = fixture.build {
					location.setField("module", innerProject)
					location.setField("file", rootProject.file("build.gradle"))
				}

				val sut = LocationViewModel(fixtViolation)

				assertFalse(sut.isLocationExternal)
			}

			@Test fun `file in the parent module is internal`() {
				val fixtViolation: Violation = fixture.build {
					location.setField("module", innerProject)
					location.setField("file", parentProject.file("build.gradle"))
				}

				val sut = LocationViewModel(fixtViolation)

				assertFalse(sut.isLocationExternal)
			}

			@Test fun `file outside the root module is external for the project`() {
				val fixtViolation: Violation = fixture.build {
					location.setField("module", innerProject)
					location.setField("file", innerProject.rootDir.resolve("../some.file"))
				}

				val sut = LocationViewModel(fixtViolation)

				assertTrue(sut.isLocationExternal)
			}

			@Test fun `file outside the root module is external the root project`() {
				val fixtViolation: Violation = fixture.build {
					location.setField("module", rootProject)
					location.setField("file", rootProject.rootDir.resolve("../some.file"))
				}

				val sut = LocationViewModel(fixtViolation)

				assertTrue(sut.isLocationExternal)
			}

			@OptIn(ExperimentalStdlibApi::class)
			@EnabledOnOs(OS.WINDOWS)
			@Test fun `file on a different drive is external the project`() {
				val fixtViolation: Violation = fixture.build {
					location.setField("module", rootProject)
					val fileInProject = rootProject.file("folder/some.file").absolutePath
					assumeFalse(fileInProject.startsWith("A:") || fileInProject.startsWith("a:")) {
						"The file ${fileInProject} is located on a drive different than A:"
					}
					location.setField("file", File(fileInProject.replaceFirstChar { 'A' }))
				}

				val sut = LocationViewModel(fixtViolation)

				assertTrue(sut.isLocationExternal)
			}
		}

		@Nested
		inner class locationRelativeTo {

			@Test fun `file in the root module from root module`() {
				val fixtViolation: Violation = fixture.build {
					location.setField("module", rootProject)
					location.setField("file", rootProject.file("build.gradle"))
				}

				val sut = LocationViewModel(fixtViolation)

				assertEquals(".${File.separator}", sut.locationRelativeToProject)
				assertEquals(".${File.separator}", sut.locationRelativeToModule)
			}

			@Test fun `file in the root module from parent module`() {
				val fixtViolation: Violation = fixture.build {
					location.setField("module", parentProject)
					location.setField("file", rootProject.file("build.gradle"))
				}

				val sut = LocationViewModel(fixtViolation)

				assertEquals(".${File.separator}", sut.locationRelativeToProject)
				assertEquals("..${File.separator}", sut.locationRelativeToModule)
			}

			@Test fun `file in the root module from inner module`() {
				val fixtViolation: Violation = fixture.build {
					location.setField("module", innerProject)
					location.setField("file", rootProject.file("build.gradle"))
				}

				val sut = LocationViewModel(fixtViolation)

				assertEquals(".${File.separator}", sut.locationRelativeToProject)
				assertEquals("..${File.separator}..${File.separator}", sut.locationRelativeToModule)
			}

			@Test fun `file in the parent module from root module`() {
				val fixtViolation: Violation = fixture.build {
					location.setField("module", rootProject)
					location.setField("file", parentProject.file("build.gradle"))
				}

				val sut = LocationViewModel(fixtViolation)

				assertEquals("sub1${File.separator}", sut.locationRelativeToProject)
				assertEquals("sub1${File.separator}", sut.locationRelativeToModule)
			}

			@Test fun `file in the parent module from parent module`() {
				val fixtViolation: Violation = fixture.build {
					location.setField("module", parentProject)
					location.setField("file", parentProject.file("build.gradle"))
				}

				val sut = LocationViewModel(fixtViolation)

				assertEquals("sub1${File.separator}", sut.locationRelativeToProject)
				assertEquals(".${File.separator}", sut.locationRelativeToModule)
			}

			@Test fun `file in the parent module from inner module`() {
				val fixtViolation: Violation = fixture.build {
					location.setField("module", innerProject)
					location.setField("file", parentProject.file("build.gradle"))
				}

				val sut = LocationViewModel(fixtViolation)

				assertEquals("sub1${File.separator}", sut.locationRelativeToProject)
				assertEquals("..${File.separator}", sut.locationRelativeToModule)
			}

			@Test fun `file in the module from root module`() {
				val fixtViolation: Violation = fixture.build {
					location.setField("module", rootProject)
					location.setField("file", innerProject.file("build.gradle"))
				}

				val sut = LocationViewModel(fixtViolation)

				assertEquals("sub1${File.separator}sub2${File.separator}", sut.locationRelativeToProject)
				assertEquals("sub1${File.separator}sub2${File.separator}", sut.locationRelativeToModule)
			}

			@Test fun `file in the module from parent module`() {
				val fixtViolation: Violation = fixture.build {
					location.setField("module", parentProject)
					location.setField("file", innerProject.file("build.gradle"))
				}

				val sut = LocationViewModel(fixtViolation)

				assertEquals("sub1${File.separator}sub2${File.separator}", sut.locationRelativeToProject)
				assertEquals("sub2${File.separator}", sut.locationRelativeToModule)
			}

			@Test fun `file in the module from inner module`() {
				val fixtViolation: Violation = fixture.build {
					location.setField("module", innerProject)
					location.setField("file", innerProject.file("build.gradle"))
				}

				val sut = LocationViewModel(fixtViolation)

				assertEquals("sub1${File.separator}sub2${File.separator}", sut.locationRelativeToProject)
				assertEquals(".${File.separator}", sut.locationRelativeToModule)
			}

			@Test fun `file outside the root module from root module`() {
				val fixtViolation: Violation = fixture.build {
					location.setField("module", rootProject)
					location.setField("file", rootProject.rootDir.resolve("../some.file"))
				}

				val sut = LocationViewModel(fixtViolation)

				assertEquals(fixtViolation.location.file.absolutePath, sut.locationRelativeToProject)
				assertEquals(fixtViolation.location.file.absolutePath, sut.locationRelativeToModule)
			}

			@Test fun `file outside the root module from parent module`() {
				val fixtViolation: Violation = fixture.build {
					location.setField("module", parentProject)
					location.setField("file", innerProject.rootDir.resolve("../some.file"))
				}

				val sut = LocationViewModel(fixtViolation)

				assertEquals(fixtViolation.location.file.absolutePath, sut.locationRelativeToProject)
				assertEquals(fixtViolation.location.file.absolutePath, sut.locationRelativeToModule)
			}

			@Test fun `file outside the root module from inner module`() {
				val fixtViolation: Violation = fixture.build {
					location.setField("module", innerProject)
					location.setField("file", innerProject.rootDir.resolve("../some.file"))
				}

				val sut = LocationViewModel(fixtViolation)

				assertEquals(fixtViolation.location.file.absolutePath, sut.locationRelativeToProject)
				assertEquals(fixtViolation.location.file.absolutePath, sut.locationRelativeToModule)
			}

			@OptIn(ExperimentalStdlibApi::class)
			@EnabledOnOs(OS.WINDOWS)
			@Test fun `file on a different drive`() {
				val fixtViolation: Violation = fixture.build {
					location.setField("module", rootProject)
					val fileInProject = rootProject.file("folder/some.file").absolutePath
					assumeFalse(fileInProject.startsWith("A:") || fileInProject.startsWith("a:")) {
						"The file ${fileInProject} is located on a drive different than A:"
					}
					location.setField("file", File(fileInProject.replaceFirstChar { 'A' }))
				}

				val sut = LocationViewModel(fixtViolation)

				assertEquals(fixtViolation.location.file.absolutePath, sut.locationRelativeToProject)
				assertEquals(fixtViolation.location.file.absolutePath, sut.locationRelativeToModule)
			}
		}
	}
}
