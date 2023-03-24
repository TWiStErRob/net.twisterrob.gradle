package net.twisterrob.gradle.internal.android

import com.android.build.api.variant.Component
import com.android.build.api.variant.Variant
import com.android.build.api.variant.impl.HasAndroidTest
import com.android.build.api.variant.impl.VariantImpl

val Variant.componentsCompat70x: List<Component>
	get() = listOfNotNull(
		this,
		this.unitTest,
		(this as? HasAndroidTest)?.androidTest,
		(this as VariantImpl).testFixturesComponent
	)
