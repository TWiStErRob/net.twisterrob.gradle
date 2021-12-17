package com.android.build.gradle.internal.api

import com.android.build.gradle.internal.variant.BaseVariantData

@Suppress("DEPRECATION" /* AGP 7.0 */)
internal val com.android.build.gradle.api.BaseVariant.variantData: BaseVariantData?
	get() = (this as? BaseVariantImpl)?.variantData

@Suppress("DEPRECATION" /* AGP 7.0 */)
internal val com.android.build.gradle.api.BaseVariant.androidTestVariantData: BaseVariantData?
	get() = ((this as? TestedVariant)?.testVariant as? BaseVariantImpl)?.variantData

@Suppress("DEPRECATION" /* AGP 7.0 */)
internal val com.android.build.gradle.api.BaseVariant.unitTestVariantData: BaseVariantData?
	get() = ((this as? TestedVariant)?.unitTestVariant as? BaseVariantImpl)?.variantData
