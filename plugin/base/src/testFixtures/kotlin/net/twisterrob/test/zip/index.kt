package net.twisterrob.test.zip

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.greaterThan
import org.hamcrest.TypeSafeMatcher
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

fun withSize(sizeMatcher: Matcher<Long>): Matcher<ZipEntry> =
	object : TypeSafeMatcher<ZipEntry>() {
		override fun describeTo(description: Description) {
			description.appendText("with size ").appendDescriptionOf(sizeMatcher)
		}

		override fun matchesSafely(item: ZipEntry): Boolean =
			sizeMatcher.matches(item.size)
	}

fun hasZipEntry(
	entryPath: String,
	entryMatcher: Matcher<ZipEntry>? = withSize(greaterThan(0L))
): Matcher<File> =
	object : TypeSafeMatcher<File>() {
		override fun describeTo(description: Description) {
			description.appendText("a zip file with an entry named '${entryPath}' and ")
				.appendDescriptionOf(entryMatcher)
		}

		@Suppress("FoldInitializerAndIfToElvis")
		override fun matchesSafely(zip: File): Boolean {
			val entry: ZipEntry? = ZipFile(zip).getEntry(entryPath)
			if (entry == null) {
				// entry not found
				return false
			}
			if (entryMatcher?.matches(entry) == false) {
				// entry doesn't match
				return false
			}
			return true
		}
	}

fun hasEntryCount(entryCountMatcher: Matcher<Int>): Matcher<File> =
	object : TypeSafeMatcher<File>() {
		override fun describeTo(description: Description) {
			description.appendText("a zip file with entry count ").appendDescriptionOf(entryCountMatcher)
		}

		override fun matchesSafely(zip: File): Boolean =
			entryCountMatcher.matches(ZipFile(zip).size())
	}
