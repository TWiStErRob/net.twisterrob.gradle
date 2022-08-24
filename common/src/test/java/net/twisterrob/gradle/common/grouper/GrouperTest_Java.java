package net.twisterrob.gradle.common.grouper;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import kotlin.Pair;
import kotlin.collections.MapsKt;
import net.twisterrob.gradle.common.grouper.TFO.E;
import net.twisterrob.gradle.common.grouper.TFO.F;
import net.twisterrob.gradle.common.grouper.TFO.G;
import org.jetbrains.annotations.NotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

@SuppressWarnings("RedundantTypeArguments")
public class GrouperTest_Java {

	@Test public void groupOn1Level() {
		Grouper.Start<TFO> grouper = Grouper.create(list(
				TFO.E1F1G1,
				TFO.E1F2G1,
				TFO.E2F1G2,
				TFO.E2F2G2
		));
		Grouper<?, List<TFO>> e = grouper.<E>by("e");
		Map<?, List<TFO>> grouping = e.group();
		assertEquals(map(
				entry(E.E1, list(TFO.E1F1G1, TFO.E1F2G1)),
				entry(E.E2, list(TFO.E2F1G2, TFO.E2F2G2))
		), grouping);
	}

	@Test public void groupOn2Levels() {
		Grouper.Start<TFO> grouper = Grouper.create(list(
				TFO.E1F1G1,
				TFO.E1F2G1,
				TFO.E1F2G2,
				TFO.E2F1G2,
				TFO.E2F2G2,
				TFO.E2F2G2
		));
		Grouper<?, List<TFO>> e = grouper.<E>by("e");
		Grouper<?, Map<?, List<TFO>>> f = e.<F>by("f");
		Map<?, Map<?, List<TFO>>> grouping = f.group();
		assertEquals(map(
				entry(E.E1, map(
						entry(F.F1, list(TFO.E1F1G1)),
						entry(F.F2, list(TFO.E1F2G1, TFO.E1F2G2))
				)),
				entry(E.E2, map(
						entry(F.F1, list(TFO.E2F1G2)),
						entry(F.F2, list(TFO.E2F2G2, TFO.E2F2G2))
				))
		), grouping);
	}

	@Test public void countOn2Levels() {
		Grouper.Start<TFO> grouper = Grouper.create(list(
				TFO.E1F1G1,
				TFO.E1F2G1,
				TFO.E1F2G2,
				TFO.E2F1G1,
				TFO.E2F1G2,
				TFO.E2F2G2
		));
		Grouper.Counter<TFO, Integer> count = grouper.count();
		Grouper<?, Integer> e = count.<E>by("e");
		Grouper<?, Map<?, Integer>> f = e.<F>by("f");
		Map<?, Map<?, Integer>> grouping = f.group();
		assertEquals(map(
				entry(E.E1, map(
						entry(F.F1, 1),
						entry(F.F2, 2)
				)),
				entry(E.E2, map(
						entry(F.F1, 2),
						entry(F.F2, 1)
				))
		), grouping);
	}

	@Test public void countOn2Levels_custom() {
		Collector<TFO, ?, Integer> multiplicator =
				Collectors.reducing(1, (TFO tfo) -> tfo.toString().length(), (a, b) -> a * b);
		Grouper.Start<TFO> grouper = Grouper.create(list(
				TFO.E1F1G1,
				TFO.E1F2G1,
				TFO.E1F2G2,
				TFO.E2F1G1,
				TFO.E2F1G2,
				TFO.E2F2G2
		), multiplicator);
		Grouper.Counter<TFO, Integer> count = grouper.count();
		Grouper<?, Integer> e = count.<E>by("e");
		Grouper<?, Map<?, Integer>> f = e.<F>by("f");
		Map<?, Map<?, Integer>> counts = f.group();
		assertEquals(map(
				entry(E.E1, map(
						entry(F.F1, 8),
						entry(F.F2, 64)
				)),
				entry(E.E2, map(
						entry(F.F1, 64),
						entry(F.F2, 8)
				))
		), counts);
	}

	@Test public void groupOn3Levels() {
		Grouper.Start<TFO> grouper = Grouper.create(list(
				TFO.E1F1G1,
				TFO.E1F2G1,
				TFO.E2F1G2,
				TFO.E2F2G2
		));
		Grouper<?, List<TFO>> e = grouper.<E>by("e");
		Grouper<?, Map<?, List<TFO>>> f = e.<F>by("f");
		Grouper<?, Map<?, Map<?, List<TFO>>>> g = f.<G>by("g");
		Map<?, Map<?, Map<?, List<TFO>>>> grouping = g.group();
		assertEquals(threeLevelGrouping(), grouping);
	}

	private Map<Object, Object> threeLevelGrouping() {
		return map(
				entry(E.E1, map(
						entry(F.F1, map(
								entry(G.G1, list(TFO.E1F1G1))
						)),
						entry(F.F2, map(
								entry(G.G1, list(TFO.E1F2G1))
						))
				)),
				entry(E.E2, map(
						entry(F.F1, map(
								entry(G.G2, list(TFO.E2F1G2))
						)),
						entry(F.F2, map(
								entry(G.G2, list(TFO.E2F2G2))
						))
				))
		);
	}

	@Test public void groupOn3LevelsDirect() {
		Grouper.Start<TFO> grouper = Grouper.create(list(
				TFO.E1F1G1,
				TFO.E1F2G1,
				TFO.E2F1G2,
				TFO.E2F2G2
		));
		Map<?, Map<?, Map<?, List<TFO>>>> counts = grouper.<E>by("e").<F>by("f").<G>by("g").group();
		assertEquals(threeLevelGrouping(), counts);
	}

	@Test public void countOn3Levels() {
		Grouper.Start<TFO> grouper = Grouper.create(list(
				TFO.E1F2G1,
				TFO.E1F1G1,
				TFO.E1F2G1,
				TFO.E2F1G2,
				TFO.E1F2G1,
				TFO.E2F2G2,
				TFO.E2F2G2
		));
		Grouper.Counter<TFO, Integer> counter = grouper.count();
		Grouper<?, Integer> e = counter.<E>by("e");
		Grouper<?, Map<?, Integer>> f = e.<F>by("f");
		Grouper<?, Map<?, Map<?, Integer>>> g = f.<G>by("g");
		Map<?, Map<?, Map<?, Integer>>> counts = g.group();
		assertEquals(map(
				entry(E.E1, map(
						entry(F.F1, map(
								entry(G.G1, 1)
						)),
						entry(F.F2, map(
								entry(G.G1, 3)
						))
				)),
				entry(E.E2, map(
						entry(F.F1, map(
								entry(G.G2, 1)
						)),
						entry(F.F2, map(
								entry(G.G2, 2)
						))
				))
		), counts);
	}

	@SuppressWarnings("varargs")
	@SafeVarargs
	private static @NotNull <T> List<T> list(T... args) {
		return Arrays.asList(args);
	}

	@SafeVarargs
	@SuppressWarnings("varargs")
	private static @NotNull <K, V> Map<K, V> map(Pair<K, V>... entries) {
		return MapsKt.mapOf(entries);
	}

	private static <K, V> @NotNull Pair<K, V> entry(K k, V v) {
		return new Pair<>(k, v);
	}
}
