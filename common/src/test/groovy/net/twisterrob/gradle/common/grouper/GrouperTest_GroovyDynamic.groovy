package net.twisterrob.gradle.common.grouper

import groovy.transform.CompileDynamic
import org.junit.Ignore
import org.junit.Test

@CompileDynamic
@Ignore("Why doesn't it work, what other than getAt do I need?")
class GrouperTest_GroovyDynamic {

	@Test void groupOn1Level() {
		def result = Grouper.create([ TFO.E1F1G1, TFO.E1F2G1, TFO.E2F1G2, TFO.E2F2G2 ])
		println result.grouper.e.group()
	}

	@Test void groupOn2Levels() {
		def result = Grouper.create([ TFO.E1F1G1, TFO.E1F2G1, TFO.E2F1G2, TFO.E2F2G2 ])
		println result.grouper.e.f.group()
	}

	@Test void groupOn3Levels() {
		def result = Grouper.create([ TFO.E1F1G1, TFO.E1F2G1, TFO.E2F1G2, TFO.E2F2G2 ])
		println result.grouper.e.f.g.group()
	}
}
