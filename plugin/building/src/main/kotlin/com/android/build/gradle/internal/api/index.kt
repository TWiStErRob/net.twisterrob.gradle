package com.android.build.gradle.internal.api

import com.android.build.gradle.internal.variant.BaseVariantData

@Suppress("DEPRECATION" /* AGP 7.0 */)
internal val com.android.build.gradle.api.BaseVariant.variantData: BaseVariantData
	get() = (this as BaseVariantImpl).variantData

@Suppress("DEPRECATION" /* AGP 7.0 */)
internal val com.android.build.gradle.api.BaseVariant.productionVariant: BaseVariantImpl
	get() = this as BaseVariantImpl

@Suppress("DEPRECATION" /* AGP 7.0 */)
internal val com.android.build.gradle.api.BaseVariant.androidTestVariant: BaseVariantImpl?
	@Suppress("CastToNullableType")
	get() = (this as TestedVariant).testVariant as BaseVariantImpl?

@Suppress("DEPRECATION" /* AGP 7.0 */)
internal val com.android.build.gradle.api.BaseVariant.unitTestVariant: BaseVariantImpl?
	// STOPSHIP REPORT CastToNullableType reports too wide
	@Suppress("CastToNullableType")
	get() = (this as TestedVariant).unitTestVariant as BaseVariantImpl?
