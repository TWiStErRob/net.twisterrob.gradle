package net.twisterrob.gradle.common.grouper

/**
 * Three Fields Object.
 */
internal class TFO private constructor(
	private val e: E,
	private val f: F,
	private val g: G
) {

	override fun toString(): String =
		String.format("%s-%s-%s", e, f, g)

	companion object {

		@JvmField val E1F1G1: TFO = TFO(E.E1, F.F1, G.G1)
		@JvmField val E1F1G2: TFO = TFO(E.E1, F.F1, G.G2)
		@JvmField val E1F2G1: TFO = TFO(E.E1, F.F2, G.G1)
		@JvmField val E1F2G2: TFO = TFO(E.E1, F.F2, G.G2)
		@JvmField val E2F1G1: TFO = TFO(E.E2, F.F1, G.G1)
		@JvmField val E2F1G2: TFO = TFO(E.E2, F.F1, G.G2)
		@JvmField val E2F2G1: TFO = TFO(E.E2, F.F2, G.G1)
		@JvmField val E2F2G2: TFO = TFO(E.E2, F.F2, G.G2)
	}

	internal enum class E {
		E1,
		E2
	}

	internal enum class F {
		F1,
		F2
	}

	internal enum class G {
		G1,
		G2
	}
}
