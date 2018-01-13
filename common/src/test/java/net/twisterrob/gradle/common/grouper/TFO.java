package net.twisterrob.gradle.common.grouper;

/**
 * Three Fields Object.
 */
class TFO {

	public static final TFO E1F1G1 = new TFO(E.E1, F.F1, G.G1);
	public static final TFO E1F1G2 = new TFO(E.E1, F.F1, G.G2);
	public static final TFO E1F2G1 = new TFO(E.E1, F.F2, G.G1);
	public static final TFO E1F2G2 = new TFO(E.E1, F.F2, G.G2);
	public static final TFO E2F1G1 = new TFO(E.E2, F.F1, G.G1);
	public static final TFO E2F1G2 = new TFO(E.E2, F.F1, G.G2);
	public static final TFO E2F2G1 = new TFO(E.E2, F.F2, G.G1);
	public static final TFO E2F2G2 = new TFO(E.E2, F.F2, G.G2);

	public final E e;
	public final F f;
	public final G g;

	TFO(E e, F f, G g) {
		this.e = e;
		this.f = f;
		this.g = g;
	}

	@Override public String toString() {
		return String.format("%s-%s-%s", e, f, g);
	}

	enum E {
		E1,
		E2
	}

	enum F {
		F1,
		F2
	}

	enum G {
		G1,
		G2
	}
}
