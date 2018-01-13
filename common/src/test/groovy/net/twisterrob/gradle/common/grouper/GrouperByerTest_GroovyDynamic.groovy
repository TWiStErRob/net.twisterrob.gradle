package net.twisterrob.gradle.common.grouper

import groovy.transform.CompileDynamic
import org.junit.Ignore
import org.junit.Test

@CompileDynamic
@Ignore("Why doesn't it work, what other than getAt do I need?")
class GrouperByerTest_GroovyDynamic {

	@Test void groupOn1Level() {
		def byer = GrouperByer.group(Arrays.asList(
				TFO.E1F1G1,
				TFO.E1F2G1,
				TFO.E2F1G2,
				TFO.E2F2G2
		))
		println byer.e.build()
	}

	@Test void groupOn2Levels() {
		def byer = GrouperByer.group(Arrays.asList(
				TFO.E1F1G1,
				TFO.E1F2G1,
				TFO.E2F1G2,
				TFO.E2F2G2
		))
		println byer.e.f.build()
	}

	@Test void groupOn3Levels() {
		def byer = GrouperByer.group(Arrays.asList(
				TFO.E1F1G1,
				TFO.E1F2G1,
				TFO.E2F1G2,
				TFO.E2F2G2
		))
		println byer.e.f.g.build()
	}
}
