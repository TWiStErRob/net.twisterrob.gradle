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
public class GrouperByerTest_Java {

	@Test
	public void groupOn1Level() {
		GrouperByer.Chain<TFO> byer = GrouperByer.group(list(
				TFO.E1F1G1,
				TFO.E1F2G1,
				TFO.E2F1G2,
				TFO.E2F2G2
		));
		GrouperByer<?, List<TFO>> e = byer.<E>getAt("e");
		assertEquals(map()
				.put(E.E1, list(TFO.E1F1G1, TFO.E1F2G1))
				.put(E.E2, list(TFO.E2F1G2, TFO.E2F2G2))
				.build(), e.build());
	}

	@Test
	public void groupOn2Levels() {
		GrouperByer.Chain<TFO> byer = GrouperByer.group(list(
				TFO.E1F1G1,
				TFO.E1F2G1,
				TFO.E2F1G2,
				TFO.E2F2G2
		));
		GrouperByer<?, List<TFO>> e = byer.<E>getAt("e");
		GrouperByer<?, Map<?, List<TFO>>> f = e.<F>getAt("f");
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
				.build(), f.build());
	}

	@Test
	public void groupOn3Levels() {
		GrouperByer.Chain<TFO> byer = GrouperByer.group(list(
				TFO.E1F1G1,
				TFO.E1F2G1,
				TFO.E2F1G2,
				TFO.E2F2G2
		));
		GrouperByer<?, List<TFO>> e = byer.<E>getAt("e");
		GrouperByer<?, Map<?, List<TFO>>> f = e.<F>getAt("f");
		GrouperByer<?, Map<?, Map<?, List<TFO>>>> g = f.<G>getAt("g");
		assertEquals(threeLevelGrouping(), g.build());
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
		GrouperByer.Chain<TFO> byer = GrouperByer.group(list(
				TFO.E1F1G1,
				TFO.E1F2G1,
				TFO.E2F1G2,
				TFO.E2F2G2
		));
		;
		GrouperByer<?, Map<?, Map<?, List<TFO>>>> g = byer.<E>getAt("e").<F>getAt("f").<G>getAt("g");
		assertEquals(threeLevelGrouping(), g.build());
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
