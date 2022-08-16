package net.twisterrob.test

import org.junit.jupiter.api.TestInfo

val TestInfo.testName: String
	get() = testClass.map { it.name }.orElse("") + "." + testMethod.map { it.name }.orElse("")
