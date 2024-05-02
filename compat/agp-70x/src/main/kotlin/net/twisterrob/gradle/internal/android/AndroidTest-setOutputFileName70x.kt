package net.twisterrob.gradle.internal.android

import com.android.build.api.component.impl.ComponentImpl
import com.android.build.api.variant.AndroidTest
import org.gradle.api.provider.Provider

fun AndroidTest.setOutputFileName70x(apkName: Provider<String>) {
	(this as ComponentImpl)
		.outputs
		.single()
		.outputFileName
		.set(apkName)
}
