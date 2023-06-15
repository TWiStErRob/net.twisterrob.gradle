package net.twisterrob.gradle.logging

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.Test

class JavaPackageHierarchyBasedMapTest {
	@Test fun `no name`() {
		val map = JavaPackageHierarchyBasedMap<Int>(emptyMap())

		val value = map.pickFor("")

		assertThat(value, nullValue())
	}

	@Test fun `no mapping`() {
		val map = JavaPackageHierarchyBasedMap<Int>(emptyMap())

		val value = map.pickFor("foo")

		assertThat(value, nullValue())
	}

	@Test fun `exact match`() {
		val map = JavaPackageHierarchyBasedMap(mapOf("foo" to 42))

		val value = map.pickFor("foo")

		assertThat(value, equalTo(42))
	}

	@Test fun `no match`() {
		val map = JavaPackageHierarchyBasedMap(mapOf("foo" to 42))

		val value = map.pickFor("bar")

		assertThat(value, nullValue())
	}

	@Test fun `prefix match`() {
		val map = JavaPackageHierarchyBasedMap(mapOf("foo" to 42))

		val value = map.pickFor("foo.bar")

		assertThat(value, equalTo(42))
	}

	@Test fun `deep prefix match`() {
		val map = JavaPackageHierarchyBasedMap(mapOf("foo" to 42))

		val value = map.pickFor("foo.bar.baz")

		assertThat(value, equalTo(42))
	}

	@Test fun `deep mapping prefix match`() {
		val map = JavaPackageHierarchyBasedMap(mapOf("foo.bar" to 42))

		val value = map.pickFor("foo.bar.baz.qux")

		assertThat(value, equalTo(42))
	}

	@Test fun `exact match wins over prefix match`() {
		val value1 = 13
		val value2 = 42
		val map = JavaPackageHierarchyBasedMap(
			mapOf(
				"foo" to value1,
				"foo.bar" to value2,
			)
		)

		val value = map.pickFor("foo.bar")

		assertThat(value, equalTo(42))
	}
}
