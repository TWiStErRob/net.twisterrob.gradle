package net.twisterrob.gradle.test

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class IndexKtTest {

	@Nested
	inner class TasksIn {

		@Test fun `nothing for nothing`() {
			val tasks = tasksIn(emptyArray())

			val expected = emptyArray<String>()
			assertArrayEquals(expected, tasks)
		}

		@Test fun `nothing for no modules`() {
			val tasks = tasksIn(emptyArray(), "task")

			val expected = emptyArray<String>()
			assertArrayEquals(expected, tasks)
		}

		@Test fun `nothing for no tasks`() {
			val tasks = tasksIn(arrayOf(":module"))

			val expected = emptyArray<String>()
			assertArrayEquals(expected, tasks)
		}

		@Test fun `combines module and task name`() {
			val tasks = tasksIn(arrayOf(":module"), "task")

			val expected = arrayOf(
				":module:task",
			)
			assertArrayEquals(expected, tasks)
		}

		@Test fun `combines tasks for each module`() {
			val tasks = tasksIn(arrayOf(":module1", ":module2"), "task")

			val expected = arrayOf(
				":module1:task",
				":module2:task",
			)
			assertArrayEquals(expected, tasks)
		}

		@Test fun `combines multiple tasks for a module`() {
			val tasks = tasksIn(arrayOf(":module"), "task1", "task2")

			val expected = arrayOf(
				":module:task1",
				":module:task2",
			)
			assertArrayEquals(expected, tasks)
		}

		@Test fun `combines multiple tasks and modules`() {
			val tasks = tasksIn(arrayOf(":module1", ":module2"), "task1", "task2")

			val expected = arrayOf(
				":module1:task1", ":module1:task2",
				":module2:task1", ":module2:task2"
			)
			assertArrayEquals(expected, tasks)
		}

		@Test fun `supports root module`() {
			val tasks = tasksIn(arrayOf(":"), "task")

			val expected = arrayOf(
				":task",
			)
			assertArrayEquals(expected, tasks)
		}

		@Test fun `fails on empty module name`() {
			assertThrows<IllegalArgumentException> {
				tasksIn(arrayOf(""), "task")
			}
		}

		@Test fun `fails on empty task name`() {
			assertThrows<IllegalArgumentException> {
				tasksIn(arrayOf(":module"), "")
			}
		}
	}
}
