package net.twisterrob.gradle.common.grouper

import groovy.transform.CompileStatic
import net.twisterrob.gradle.common.grouper.TFO.E
import net.twisterrob.gradle.common.grouper.TFO.F
import net.twisterrob.gradle.common.grouper.TFO.G
import org.junit.Test

@CompileStatic
class GrouperByerTest_GroovyStatic {

	@Test void groupOn1Level() {
		def byer = GrouperByer.group(Arrays.asList(
				TFO.E1F1G1,
				TFO.E1F2G1,
				TFO.E2F1G2,
				TFO.E2F2G2
		))
		assert byer['e'].build() == [
				(E.E1): [ TFO.E1F1G1, TFO.E1F2G1 ],
				(E.E2): [ TFO.E2F1G2, TFO.E2F2G2 ]
		]
	}

	@Test void groupOn2Levels() {
		def byer = GrouperByer.group(Arrays.asList(
				TFO.E1F1G1,
				TFO.E1F2G1,
				TFO.E2F1G2,
				TFO.E2F2G2
		))
		assert byer['e']['f'].build() == [
				(E.E1): [
						(F.F1): [ TFO.E1F1G1 ],
						(F.F2): [ TFO.E1F2G1 ]
				],
				(E.E2): [
						(F.F1): [ TFO.E2F1G2 ],
						(F.F2): [ TFO.E2F2G2 ]
				],
		]
	}

	@Test void groupOn3Levels() {
		def byer = GrouperByer.group(Arrays.asList(
				TFO.E1F1G1,
				TFO.E1F2G1,
				TFO.E2F1G2,
				TFO.E2F2G2
		))
		assert byer['e']['f']['g'].build() == [
				(E.E1): [
						(F.F1): [
								(G.G1): [ TFO.E1F1G1 ]
						],
						(F.F2): [
								(G.G1): [ TFO.E1F2G1 ]
						]
				],
				(E.E2): [
						(F.F1): [
								(G.G2): [ TFO.E2F1G2 ]
						],
						(F.F2): [
								(G.G2): [ TFO.E2F2G2 ]
						]
				],
		]
	}
}
