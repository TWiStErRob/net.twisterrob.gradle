package net.twisterrob.gradle.android;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.jakewharton.dex.DexMethod;
import com.jakewharton.dex.TypeDescriptor;
import org.jetbrains.annotations.NotNull;

class GradleTestHelpers {

	/**
	 * Replacement for {@link DexMethod#declaringType} + {@link TypeDescriptor#sourceName} chained together.
	 * This should work from Kotlin as {@code item.declaringType.sourceName}, but for some reason
	 * the inline class name mangling is not compatible with dex-member-list since updating to Kotlin 1.4.32.
	 * <pre>
	 * java.lang.NoSuchMethodError: com.jakewharton.dex.DexMethod.getDeclaringType-YNYzXYw()Ljava/lang/String;
	 *     at net.twisterrob.gradle.android.DexMethodTypeSafeMatcher.matchesSafely(GradleTestHelpers.kt:189)
	 * </pre>
	 * <p>
	 * Need to call it reflectively since Java can't see inline methods,
	 * because {@link TypeDescriptor} is not {@link kotlin.jvm.JvmInline} and also project's JVM level is 1.8.
	 */
	@SuppressWarnings("JavadocReference")
	static @NotNull String sourceName(@NotNull DexMethod item) {
		try {
			Method sourceName = TypeDescriptor.class.getDeclaredMethod("getSourceName-impl", String.class);
			return (String) sourceName.invoke(null, item.getDeclaringType());
		} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}
}
