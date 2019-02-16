package net.twisterrob.gradle.test

inline fun <T> runBuild(block: () -> T): T = block()
