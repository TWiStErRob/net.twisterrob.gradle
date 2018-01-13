package net.twisterrob.gradle.common.grouper;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;

import net.twisterrob.gradle.common.grouper.TFO.E;
import net.twisterrob.gradle.common.grouper.TFO.F;
import net.twisterrob.gradle.common.grouper.TFO.G;

@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
public class GrouperTest_Java {

	@Test
	public void groupOn1Level() {
		Grouper.Chain<TFO> grouper = Grouper.create(list(
				TFO.E1F1G1,
				TFO.E1F2G1,
				TFO.E2F1G2,
				TFO.E2F2G2
		)).getGrouper();
		Grouper<?, List<TFO>> e = grouper.<E>by("e");
		assertEquals(map()
				.put(E.E1, list(TFO.E1F1G1, TFO.E1F2G1))
				.put(E.E2, list(TFO.E2F1G2, TFO.E2F2G2))
				.build(), e.group());
	}

	@Test
	public void groupOn2Levels() {
		Grouper.Chain<TFO> grouper = Grouper.create(list(
				TFO.E1F1G1,
				TFO.E1F2G1,
				TFO.E2F1G2,
				TFO.E2F2G2
		)).getGrouper();
		Grouper<?, List<TFO>> e = grouper.<E>by("e");
		Grouper<?, Map<?, List<TFO>>> f = e.<F>by("f");
		assertEquals(map()
				.put(E.E1, map()
						.put(F.F1, list(TFO.E1F1G1))
						.put(F.F2, list(TFO.E1F2G1))
						.build()
				)
				.put(E.E2, map()
						.put(F.F1, list(TFO.E2F1G2))
						.put(F.F2, list(TFO.E2F2G2))
						.build()
				)
				.build(), f.group());
	}

	@Test
	public void groupOn3Levels() {
		Grouper.Chain<TFO> grouper = Grouper.create(list(
				TFO.E1F1G1,
				TFO.E1F2G1,
				TFO.E2F1G2,
				TFO.E2F2G2
		)).getGrouper();
		Grouper<?, List<TFO>> e = grouper.<E>by("e");
		Grouper<?, Map<?, List<TFO>>> f = e.<F>by("f");
		Grouper<?, Map<?, Map<?, List<TFO>>>> g = f.<G>by("g");
		assertEquals(threeLevelGrouping(), g.group());
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

	@Test
	public void groupOn3LevelsDirect() {
		Grouper.Chain<TFO> grouper = Grouper.create(list(
				TFO.E1F1G1,
				TFO.E1F2G1,
				TFO.E2F1G2,
				TFO.E2F2G2
		)).getGrouper();
		Grouper<?, Map<?, Map<?, List<TFO>>>> g = grouper.<E>by("e").<F>by("f").<G>by("g");
		assertEquals(threeLevelGrouping(), g.group());
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
