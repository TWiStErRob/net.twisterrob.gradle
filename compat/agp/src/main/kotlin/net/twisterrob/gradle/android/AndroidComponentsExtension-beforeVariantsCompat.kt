package net.twisterrob.gradle.android

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.Variant
import com.android.build.api.variant.VariantBuilder

/**
 * Compatibility version for [AndroidComponentsExtension.beforeVariants] DSL:
 * `androidComponents.beforeVariants { }`.
 *
 * @see onVariantsCompat for explanation.
 */
fun <DslExtensionT : CommonExtension<*, *, *, *, *>, VariantBuilderT : VariantBuilder, VariantT : Variant>
		AndroidComponentsExtension<DslExtensionT, VariantBuilderT, VariantT>.beforeVariantsCompat(
	callback: (VariantBuilderT) -> Unit
) {
	this.beforeVariants(selector().all(), callback)
}
