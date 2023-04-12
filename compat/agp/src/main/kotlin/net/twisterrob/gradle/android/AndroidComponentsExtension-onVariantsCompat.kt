package net.twisterrob.gradle.android

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.Variant
import com.android.build.api.variant.VariantBuilder

/**
 * Compatibility version for [AndroidComponentsExtension.onVariants] DSL:
 * `androidComponents.onVariants { }`.
 *
 * Need to duplicate this method, because the signature changed among 7.x versions.
 * When compiling against AGP 7.4, the following error is thrown upon executing the code on AGP 7.3 and below:
 * ```text
 * java.lang.NoSuchMethodError:
 * 'void com.android.build.api.variant.AndroidComponentsExtension.onVariants$default(
 * AndroidComponentsExtension, VariantSelector, Function1, int, Object)'
 * ```
 * This is because:
 *
 * AGP 7.4 generated
 * ```java
 * @Metadata(mv = {1, 6, 0}, ...)
 * interface AndroidComponentsExtensions {
 *     static void onVariants$default(...);
 *     public static final class DefaultImpls {
 *         public static void onVariants$default(...);
 *     }
 * }
 * ```
 * while AGP 7.0 to AGP 7.3 generated
 * ```java
 * @Metadata(mv = {1, 5, 1}, ...) // or mv = {1, 4, 2}, depending on the version.
 * interface AndroidComponentsExtensions {
 *     public static final class DefaultImpls {
 *         public static void onVariants$default(...)
 *     }
 * }
 * ```
 * so when compiling against AGP 7.4, the `onVariants$default` method is called,
 * but it doesn't exist in that form on earlier versions.
 *
 * This method is essentially what @JvmOverloads would generate.
 */
fun <DslExtensionT : CommonExtension<*, *, *, *, *>, VariantBuilderT : VariantBuilder, VariantT : Variant>
		AndroidComponentsExtension<DslExtensionT, VariantBuilderT, VariantT>.onVariantsCompat(
	callback: (VariantT) -> Unit
) {
	this.onVariants(selector().all(), callback)
}
