package com.android.build.gradle.internal.api

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.variant.BaseVariantData

internal val BaseVariant.variantData: BaseVariantData?
	get() = (this as? BaseVariantImpl)?.variantData

internal val BaseVariant.androidTestVariantData: BaseVariantData?
	get() = ((this as? TestedVariant)?.testVariant as? BaseVariantImpl)?.variantData

internal val BaseVariant.unitTestVariantData: BaseVariantData?
	get() = ((this as? TestedVariant)?.unitTestVariant as? BaseVariantImpl)?.variantData
