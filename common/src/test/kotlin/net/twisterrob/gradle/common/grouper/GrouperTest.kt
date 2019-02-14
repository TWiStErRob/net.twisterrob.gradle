package net.twisterrob.gradle.common.grouper

import org.junit.Assert.assertEquals
import org.junit.Test

class GrouperTest {

	@Test fun `list nothing`() {
		val grouper = Grouper.create(emptyList<TFO>())
		assertEquals(emptyList<TFO>(), grouper.list)
	}

	@Test fun `list nothing (with Groovy sugar)`() {
		val grouper = Grouper.create(emptyList<TFO>())
		assertEquals(emptyList<TFO>(), grouper.getProperty("list"))
	}

	@Test fun `group nothing on 1 level`() {
		val grouper = Grouper.create(emptyList<TFO>())
		val e = grouper.by("e")
		val grouping = e.group()
		assertEquals(emptyMap<Any, List<TFO>>(), grouping)
	}

	@Test fun `group nothing on 1 level (with Groovy sugar)`() {
		val grouper = Grouper.create(emptyList<TFO>())
		@Suppress("UNCHECKED_CAST")
		val e = grouper.getProperty("e") as Grouper<Any, List<TFO>>
		val grouping = e.group()
		assertEquals(emptyMap<Any, List<TFO>>(), grouping)
	}

	@Test fun `group nothing on 2 levels`() {
		val grouper = Grouper.create(emptyList<TFO>())
		val e = grouper.by("e")
		val f = e.by("f")
		val grouping = f.group()
		assertEquals(emptyMap<Any, Map<Any, List<TFO>>>(), grouping)
	}
	@Test fun `group nothing on 2 levels (with Groovy sugar)`() {
		val grouper = Grouper.create(emptyList<TFO>())
		@Suppress("UNCHECKED_CAST")
		val e = grouper.getProperty("e") as Grouper<Any, List<TFO>>
		@Suppress("UNCHECKED_CAST")
		val f = e.getProperty("f") as Grouper<Any, Map<*, List<TFO>>>
		val grouping = f.group()
		assertEquals(emptyMap<Any, Map<Any, List<TFO>>>(), grouping)
	}
}
