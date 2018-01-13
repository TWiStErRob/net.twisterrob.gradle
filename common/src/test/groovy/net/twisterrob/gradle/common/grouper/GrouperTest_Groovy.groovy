package net.twisterrob.gradle.common.grouper

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import net.twisterrob.gradle.common.grouper.TFO.E
import net.twisterrob.gradle.common.grouper.TFO.F
import net.twisterrob.gradle.common.grouper.TFO.G
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

class GrouperTest_Groovy {

	Grouper.Start<TFO> sut

	@Before void setUp() {
		sut = Grouper.create([ TFO.E1F1G1, TFO.E1F2G1, TFO.E2F1G2, TFO.E2F2G2 ])
	}

	@Test void list() {
		assert sut.list == [ TFO.E1F1G1, TFO.E1F2G1, TFO.E2F1G2, TFO.E2F2G2 ]
	}

	@Ignore("Why doesn't it work, what other than getAt do I need?")
	@CompileDynamic
	@Test void groupOn1Level_dynamic() {
		assertGroupedOn1Level(sut.e)
	}

	@CompileStatic
	@Test void groupOn1Level() {
		assertGroupedOn1Level(sut['e'])
	}

	private static void assertGroupedOn1Level(Grouper<?, List<TFO>> grouper) {
		assert grouper.group() == [
				(E.E1): [ TFO.E1F1G1, TFO.E1F2G1 ],
				(E.E2): [ TFO.E2F1G2, TFO.E2F2G2 ]
		]
	}

	@Ignore("Why doesn't it work, what other than getAt do I need?")
	@CompileDynamic
	@Test void groupOn2Levels_dynamic() {
		assertGroupedOn2Level(sut.e.f)
	}

	@CompileStatic
	@Test void groupOn2Levels() {
		def grouper = sut['e']['f']
		assertGroupedOn2Level(grouper)
	}

	private static void assertGroupedOn2Level(Grouper<?, Map<?, List<TFO>>> grouper) {
		assert grouper.group() == [
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

	@Ignore("Why doesn't it work, what other than getAt do I need?")
	@CompileDynamic
	@Test void groupOn3Levels_dynamic() {
		assertGroupedOn3Levels(sut.e.f.g)
	}

	@CompileStatic
	@Test void groupOn3Levels() {
		assertGroupedOn3Levels(sut['e']['f']['g'])
	}

	private static void assertGroupedOn3Levels(Grouper<?, Map<?, Map<?, List<TFO>>>> grouper) {
		assert grouper.group() == [
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
