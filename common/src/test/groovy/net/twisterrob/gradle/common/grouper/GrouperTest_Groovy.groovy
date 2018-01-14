package net.twisterrob.gradle.common.grouper

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import net.twisterrob.gradle.common.grouper.TFO.E
import net.twisterrob.gradle.common.grouper.TFO.F
import net.twisterrob.gradle.common.grouper.TFO.G
import org.junit.Before
import org.junit.Test

class GrouperTest_Groovy {

	Grouper.Start<TFO> sut

	@Before void setUp() {
		sut = Grouper.create([ TFO.E1F1G1, TFO.E1F2G1, TFO.E2F1G2, TFO.E2F2G2 ])
	}

	@Test void getsOriginalList() {
		assert sut.list == [ TFO.E1F1G1, TFO.E1F2G1, TFO.E2F1G2, TFO.E2F2G2 ]
	}

	@CompileDynamic
	@Test void byReturnsSelf() {
		assert sut.by == sut
	}

	@CompileDynamic
	@Test(expected = MissingPropertyException) void missingPropertyFails() {
		sut.aMissingName
	}

	@CompileDynamic
	@Test void groupOn1Level_dynamic_by() {
		//noinspection GroovyAssignabilityCheck should be the same as the static version
		assertGroupedOn1Level(sut.by.e)
	}

	@CompileDynamic
	@Test void groupOn1Level_dynamic() {
		//noinspection GroovyAssignabilityCheck should be the same as the static version
		assertGroupedOn1Level(sut.e)
	}

	@CompileStatic
	@Test void groupOn1Level_static() {
		assertGroupedOn1Level(sut['e'])
	}

	private static void assertGroupedOn1Level(Grouper<?, List<TFO>> grouper) {
		assert grouper.group() == [
				(E.E1): [ TFO.E1F1G1, TFO.E1F2G1 ],
				(E.E2): [ TFO.E2F1G2, TFO.E2F2G2 ]
		]
	}

	@CompileDynamic
	@Test void groupOn2Levels_dynamic_by() {
		//noinspection GroovyAssignabilityCheck should be the same as the static version
		assertGroupedOn2Level(sut.by.e.by.f)
	}

	@CompileDynamic
	@Test void groupOn2Levels_dynamic() {
		//noinspection GroovyAssignabilityCheck should be the same as the static version
		assertGroupedOn2Level(sut.e.f)
	}

	@CompileStatic
	@Test void groupOn2Levels_static() {
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

	@CompileDynamic
	@Test void groupOn3Levels_dynamic_by() {
		//noinspection GroovyAssignabilityCheck should be the same as the static version
		assertGroupedOn3Levels(sut.by.e.by.f.by.g)
	}

	@CompileDynamic
	@Test void groupOn3Levels_dynamic() {
		//noinspection GroovyAssignabilityCheck should be the same as the static version
		assertGroupedOn3Levels(sut.e.f.g)
	}

	@CompileStatic
	@Test void groupOn3Levels_static() {
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

	@CompileDynamic
	@Test void groupOn3Levels_mixed() {
		//noinspection GrUnresolvedAccess,GroovyAssignabilityCheck should be the same as the static version
		assertGroupedOn3Levels(sut.by.e.by.by.by['f'].by.by('g'))
	}
}
