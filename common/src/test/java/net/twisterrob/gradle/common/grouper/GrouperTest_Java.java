package net.twisterrob.gradle.common.grouper;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;
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
		assertEquals(map()
				.put(E.E1, list(TFO.E1F1G1, TFO.E1F2G1))
				.put(E.E2, list(TFO.E2F1G2, TFO.E2F2G2))
				.build(), grouping);
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
		assertEquals(map()
				.put(E.E1, map()
						.put(F.F1, list(TFO.E1F1G1))
						.put(F.F2, list(TFO.E1F2G1, TFO.E1F2G2))
						.build()
				)
				.put(E.E2, map()
						.put(F.F1, list(TFO.E2F1G2))
						.put(F.F2, list(TFO.E2F2G2, TFO.E2F2G2))
						.build()
				)
				.build(), grouping);
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
		assertEquals(map()
				.put(E.E1, map()
						.put(F.F1, 1)
						.put(F.F2, 2)
						.build()
				)
				.put(E.E2, map()
						.put(F.F1, 2)
						.put(F.F2, 1)
						.build()
				)
				.build(), grouping);
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
		assertEquals(map()
				.put(E.E1, map()
						.put(F.F1, 8)
						.put(F.F2, 64)
						.build()
				)
				.put(E.E2, map()
						.put(F.F1, 64)
						.put(F.F2, 8)
						.build()
				)
				.build(), counts);
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

	private ImmutableMap<Object, Object> threeLevelGrouping() {
		return map()
				.put(E.E1, map()
						.put(F.F1, map()
								.put(G.G1, list(TFO.E1F1G1))
								.build()
						)
						.put(F.F2, map()
								.put(G.G1, list(TFO.E1F2G1))
								.build()
						)
						.build()
				)
				.put(E.E2, map()
						.put(F.F1, map()
								.put(G.G2, list(TFO.E2F1G2))
								.build()
						)
						.put(F.F2, map()
								.put(G.G2, list(TFO.E2F2G2))
								.build()
						)
						.build()
				)
				.build();
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
		assertEquals(map()
				.put(E.E1, map()
						.put(F.F1, map()
								.put(G.G1, 1)
								.build()
						)
						.put(F.F2, map()
								.put(G.G1, 3)
								.build()
						)
						.build()
				)
				.put(E.E2, map()
						.put(F.F1, map()
								.put(G.G2, 1)
								.build()
						)
						.put(F.F2, map()
								.put(G.G2, 2)
								.build()
						)
						.build()
				)
				.build(), counts);
	}

	@SuppressWarnings("varargs")
	@SafeVarargs
	private static @NotNull <T> List<T> list(T... args) {
		return Arrays.asList(args);
	}

	private static @NotNull <K, V> ImmutableMap.Builder<K, V> map() {
		return ImmutableMap.builder();
	}
}
